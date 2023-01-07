package io.github.ztianz.uid.snowflake.core.dao;

import io.github.ztianz.uid.snowflake.core.model.IdWorkerNode;

public interface IdWorkNodeDao {

    IdWorkerNode insertMysql(IdWorkerNode idWorkerNode);

    void workerNodeHeartBeat(IdWorkerNode idWorkerNode);
}
