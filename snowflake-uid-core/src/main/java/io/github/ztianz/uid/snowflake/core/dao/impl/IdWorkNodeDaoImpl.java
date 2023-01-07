package io.github.ztianz.uid.snowflake.core.dao.impl;

import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.hutool.db.handler.EntityHandler;
import io.github.ztianz.uid.snowflake.core.dao.IdWorkNodeDao;
import io.github.ztianz.uid.snowflake.core.exception.SnowErrorException;
import io.github.ztianz.uid.snowflake.core.model.IdWorkerNode;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class IdWorkNodeDaoImpl implements IdWorkNodeDao {

    private static final String TABLE_NAME="id_worker_node";

    private static final String QUERY_SQL="SELECT \n   work_id \n FROM \n" +TABLE_NAME+" \nWHERE \n   application_name = ? \nORDER BY \n   work_id DESC \nLIMIT 1 \nFOR UPDATE";

    DataSource dataSource;

    public IdWorkNodeDaoImpl(DataSource dataSource) {
        this.dataSource=dataSource;
    }

    @Override
    public IdWorkerNode insertMysql(IdWorkerNode idWorkerNode) {
        Session session = null;
        try {
            session = Session.create(dataSource);
        } catch (Exception e){
            e.printStackTrace();
            throw new SnowErrorException("数据连接为空报错");
        }
        long nextWorkId;
        Entity entity = Entity.create(TABLE_NAME)
                .set("host_name", idWorkerNode.getHostName())
                .set("application_name", idWorkerNode.getApplicationName())
                .set("server_port", idWorkerNode.getServerPort())
                .set("update_time", idWorkerNode.getUpdateTime())
                .set("create_time", idWorkerNode.getCreateTime());
        long result = -1L;
        try {
            session.beginTransaction();
            Entity resultEntity=session.query( QUERY_SQL, EntityHandler.create(),idWorkerNode.getApplicationName());

            if (null == resultEntity){
                nextWorkId=0;
            } else {
                nextWorkId=resultEntity.getLong("work_id")+1;
            }
            entity.set("work_id",nextWorkId);
            // 增，生成SQL为 INSERT INTO `table_name` SET(`字段1`, `字段2`) VALUES(?,?)
            result = session.insertForGeneratedKey(entity);
            log.info("workId:{}, primaryKey:{}",nextWorkId,result);
            session.commit();
        } catch (SQLException e) {
            session.quietRollback();
            e.printStackTrace();
            throw new SnowErrorException("get WorkerId error:" + e.getLocalizedMessage());
        }
        idWorkerNode.setWorkId(nextWorkId);
        idWorkerNode.setId(result);
        return idWorkerNode;
    }

    @Override
    public void workerNodeHeartBeat(IdWorkerNode idWorkerNode) {
        Session session = null;
        try {
            session = Session.create(dataSource);
        } catch (Exception e){
            e.printStackTrace();
            throw new SnowErrorException("数据连接为空报错");
        }
        try {
            session.beginTransaction();
            Entity entity = Entity.create(TABLE_NAME).set("update_time", LocalDateTime.now());
            Entity where = Entity.create(TABLE_NAME).set("id", idWorkerNode.getId());
            session.update(entity, where);
            session.commit();
        } catch (SQLException e) {
            session.quietRollback();
            e.printStackTrace();
            throw new SnowErrorException("get WorkerId error:" + e.getLocalizedMessage());
        }
    }
}
