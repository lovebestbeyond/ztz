package io.github.ztianz.uid.snowflake.core.service;

import cn.hutool.core.util.StrUtil;
import io.github.ztianz.uid.snowflake.core.SnowflakeIDGenImpl;
import io.github.ztianz.uid.snowflake.core.dao.IdWorkNodeDao;
import io.github.ztianz.uid.snowflake.core.dao.impl.IdWorkNodeDaoImpl;
import io.github.ztianz.uid.snowflake.core.exception.SnowErrorException;
import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SnowflakeService {

    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);

    private SnowflakeIDGenImpl idGen;

    private DruidDataSource dataSource;

    public SnowflakeService(String driverClassName, String url, String username, String password, String applicationName, Integer serverrPort, Long workerIdBits, Long sequenceBits, Long twepoch, String interfaceName, Boolean heartBeat) throws SQLException, SnowErrorException {
        if (StrUtil.isBlank(driverClassName)) {
            throw new SnowErrorException("database driverClassName can not be null");
        }
        if (StrUtil.isBlank(url)) {
            throw new SnowErrorException("database url can not be null");
        }
        if (StrUtil.isBlank(username)) {
            throw new SnowErrorException("database username can not be null");
        }
        if (StrUtil.isBlank(password)) {
            throw new SnowErrorException("database password can not be null");
        }
        if (StrUtil.isBlank(applicationName)) {
            throw new SnowErrorException("applicationName can not be null");
        }
        if (null == serverrPort) {
            throw new SnowErrorException("serverrPort can not be null");
        }
        if (null == heartBeat) {
            heartBeat = false;
        }
        if ((null == workerIdBits && null != sequenceBits) || (null != workerIdBits && null == sequenceBits)) {
            throw new SnowErrorException("workerIdBits and sequenceBits can't not Config");
        }
        if (null == twepoch) {
            twepoch = 1672502400000L;
        }
        if (null == workerIdBits) {
            workerIdBits = 16L;
        }
        if (null == sequenceBits) {
            sequenceBits = 7L;
        }
        // Config dataSource
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.init();
        // Config Dao
        IdWorkNodeDao dao = new IdWorkNodeDaoImpl(dataSource);
        // Config ID Gen
        idGen = new SnowflakeIDGenImpl(dao, applicationName, serverrPort, workerIdBits, sequenceBits, twepoch, interfaceName, heartBeat);
        logger.info("Snowflake Service Init Successfully");
    }

    public Long getId() {
        return idGen.get();
    }
}
