package io.github.ztianz.uid.snowflake.core;

import cn.hutool.core.date.DateUtil;
import io.github.ztianz.uid.snowflake.core.dao.IdWorkNodeDao;
import io.github.ztianz.uid.snowflake.core.exception.SnowErrorException;
import io.github.ztianz.uid.snowflake.core.model.IdWorkerNode;
import io.github.ztianz.uid.snowflake.core.util.SystemUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SnowflakeIDGenImpl {

    private static final String LOG_TEMPLATE="\r\n雪花算法Info:\r\n主键：{}\r\n应用名称：{}\r\n心跳周期：{}\r\n时间位长度：{}，\r\n机器位长度：{}，\r\n序列位长度：{}，\r\n起始时间：{}~~~~~{}，\r\n可用时间：{}~~~~~{}年，\r\n截止使用时间{}\r\n机器值范围：{}~~~~~当前机器值{}，\r\n每秒产生最大数据：{}个";


    private IdWorkNodeDao dao;

    private IdWorkerNode idWorkerNode;

    /**
     * 开始时间（当前时间减去开始时间的时间戳为计算id时的时间戳）
     */
    private Long twepoch;
    /**
     * 机器id所占的位数
     */
    private Long workerIdBits;
    /**
     * 支持的最大机器id，结果是1023 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     * 最小能够分配的workerid =0
     * 最大能够分配的workerid =1023
     */
    private Long maxWorkerId;
    /**
     * 序列在id中占的位数
     */
    private Long sequenceBits;
    /**
     * 机器ID向左移12位
     */
    private Long workerIdShift = sequenceBits;
    /**
     * 时间截向左移22位(10+12)
     */
    private Long timestampLeftShift;
    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private Long sequenceMask;
    /**
     * 工作机器ID(0~1023)
     * 当前机器的id
     */
    private Long workerId;
    /**
     * 毫秒内序列(0~4095)
     */
    private Long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private Long lastTimestamp = -1L;

    private static final Random RANDOM = new Random();


    //log用到
    private Long timestampBits;
    //log用到
    private Long maxTimestamp;


    public SnowflakeIDGenImpl(IdWorkNodeDao dao, String applicationName, Integer serverrPort, Long workerIdBits, Long sequenceBits, Long twepoch, Boolean heartBeat) {
        if (null == dao) {
            throw new SnowErrorException("Snowflake datasource operate error");
        }
        this.dao = dao;
        if (null != workerIdBits) {
            if (0 < workerIdBits) {
                this.workerIdBits = workerIdBits;
            } else {
                throw new SnowErrorException("workerIdBits error: " + workerIdBits);
            }
        }
        if (null != sequenceBits) {
            if (0 < sequenceBits) {
                this.sequenceBits = sequenceBits;
            } else {
                throw new SnowErrorException("sequenceBits error: " + sequenceBits);
            }
        }
        if (23 < (workerIdBits + sequenceBits) || 22 > (workerIdBits + sequenceBits)) {
            throw new SnowErrorException("workerIdBits and sequenceBits not fit(22-23): " + workerIdBits + "  " + sequenceBits);
        }
        if (null != twepoch) {
            long currentTimeMillis = timeGen();
            if (twepoch > currentTimeMillis) {
                throw new SnowErrorException("Snowflake not support twepoch gt currentTime: " + twepoch + " " + currentTimeMillis);
            }
            this.twepoch = twepoch;
        }
        this.maxWorkerId = ~(-1L << this.workerIdBits);
        this.workerIdShift = this.sequenceBits;
        this.timestampLeftShift = this.sequenceBits + this.workerIdBits;
        this.sequenceMask = ~(-1L << this.sequenceBits);
        this.timestampBits = 63L - this.workerIdBits - this.sequenceBits;
        this.maxTimestamp = ~(-1L << timestampBits);
        this.workerId = getNoteWorkerId(applicationName, serverrPort, heartBeat);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new SnowErrorException("workerId must gte 0 and lte : " + maxWorkerId + ",  currentWorkerId:" + workerId);
        }
        if (heartBeat) {
            init();
        }
        logInfo();
    }

    private Long getNoteWorkerId(String applicationName, Integer serverrPort, Boolean heartBeat) {
//        int cycleMinutes = 30 * 60 + (RANDOM.nextInt(31 * 60));
        int cycleMinutes = 30 * 60;
        IdWorkerNode WorkerNode = IdWorkerNode.builder()
                .hostName(SystemUtil.getIp())
                .serverPort(serverrPort)
                .applicationName(applicationName)
                .cycleMinutes(cycleMinutes)
                .heartBeat(heartBeat)
                .updateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        WorkerNode = dao.insertMysql(WorkerNode);
        this.idWorkerNode = WorkerNode;
        return WorkerNode.getWorkId();
    }

    public synchronized Long get() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
//                        return new SnowflakePkResult(-1L, Status.EXCEPTION);
                        throw new SnowErrorException("时钟回拨等待双倍时间重试后，依然失败");
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
//                    return new SnowflakePkResult(-2L, Status.EXCEPTION);
                    throw new SnowErrorException("时钟回拨");
                }
            } else {
//                return new SnowflakePkResult(-3L, Status.EXCEPTION);
                throw new SnowErrorException("时钟回拨大于5毫秒");
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                log.warn("********本秒id满了，从下一秒启用*****");
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = 0L + RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = 0L + RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return id;
//        return new SnowflakePkResult(id, Status.SUCCESS);
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(Long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected Long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }

    public boolean init() {
        log.info("snowflake Init ...");
        // 确保加载到kv后才初始化成功
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("snowflake-uid-heartBeat-thread");
                t.setDaemon(true);
                return t;
            }
        });
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                heartBeat();
            }
        }, this.idWorkerNode.getCycleMinutes(), this.idWorkerNode.getCycleMinutes(), TimeUnit.SECONDS);
        return true;
    }

    public void heartBeat() {
        dao.workerNodeHeartBeat(this.idWorkerNode);
        logInfo();
    }

    private void logInfo() {

        log.info(LOG_TEMPLATE,
                this.idWorkerNode.getId(),
                this.idWorkerNode.getApplicationName(),
                (this.idWorkerNode.getHeartBeat() ? ((this.idWorkerNode.getCycleMinutes() / 60) + "分钟") : ("不同步")),
                this.timestampBits,
                this.workerIdBits,
                this.sequenceBits,
                this.twepoch, DateUtil.format(DateUtil.date(this.twepoch), "yyyy-MM-dd HH:mm:ss.SSS"),
                this.maxTimestamp, (this.maxTimestamp) / (1000L * 60L * 60L * 24L * 365L),
                DateUtil.format(DateUtil.date(this.twepoch + maxTimestamp), "yyyy-MM-dd HH:mm:ss.SSS"),
                0 + "-" + this.maxWorkerId, this.workerId,
                (this.sequenceMask + 1) * 1000);
    }

    public IdWorkNodeDao getDao() {
        return dao;
    }

    public void setDao(IdWorkNodeDao dao) {
        this.dao = dao;
    }
}
