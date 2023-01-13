# snowflake-uid

snowflake-uid 介绍
snowflake-uid是基于雪花算法 在分布式环境中主键生成的一种解决方案。它实现了workerId基于mysql数据库的自动生成，保证不会重复。同时workeridBits和sequenceBits支持自定义长度（长度的自定义会影响使用时间）


#### 安装教程

1. pom中引入starter（已上传到中央仓库）
##### 
            <dependency>
                <groupId>io.github.ztianz.uid</groupId>
                <artifactId>snowflake-uid-spring-boot-starter</artifactId>
                <version>0.0.1</version>
            </dependency>


3. 加入yml配置
3. 执行数据库脚本，生产对应表
4. 注入SnowflakeService，调用 snowflake.getId()返回一个Long类型的主键值

## 使用说明

##### 1.ym配置
     ztz-uid:
     	snowflake:
     		driver-class-name: com.mysql.cj.jdbc.Driver
     		url: jdbc:mysql://127.0.0.1:3306/hao?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true
     		username: ***
     		password: ***
     		application-name: abc
     		server-port: 8888


	配置说明：
    driver-class-name        固定值（必要）
    url                      数据库连接地址（必要）
    username                 数据库连接地址（必要）
    password                 数据库连接地址（必要）
    application-name         应用名称，作为workerId自增的标识（必要）
    server-port              应用端口（必要）
    worker-id-bits           机器id位长，默念16位，可以产生的wokerid范围是0~65535（非必要）
    sequence-bits            序列位，默认7位，每秒最多产生128000个主键（非必要）
    twepoch                  开始时间的时间戳，默认1672502400000，从2023-01-01 00:00:00开始（非必要）
    heart-beat               是否启用心跳(true/false)，默认false，约半个小时更新一下数据库，记录下workerid还在使用（非必要）
    
    *******worker-id-bits和sequence-bits的长度和应是22~23.当23位时可以使用约35年，22位时可以使用约70年
    *******非必要的配置项，yml配置成功后，就不要修改了。因为修改会配置，会影响雪花id的生成，有可能会出现重复的id

##### 2.数据库脚本
    CREATE TABLE `id_worker_node`  (
    	`id` bigint(20) NOT NULL AUTO_INCREMENT,
    	`host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    	`server_port` int(20) NOT NULL,
    	`application_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    	`work_id` bigint(20) NOT NULL,
    	`update_time` datetime(3) NOT NULL,
    	`create_time` datetime(3) NOT NULL,
   		 PRIMARY KEY (`id`) USING BTREE,
    	UNIQUE INDEX `uniq_applicationName_workId`(`application_name`, `work_id`) USING BTREE
    ) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
##### 3.workerid原理
    1.当项目启动时，会根据数据库中application_name新建一个为0值作为workerid；项目停止时，当前workerid弃用；以后每次次启动都在之前的workerid的基础上加1。这样就保证了wokerid的不重复。

    2.application_name是一个特别重要的配置。同一个application_name下，不会有相同的的值，这样同一系统就可以启动多个实例也没有问题。不同的application_name之间是互相不影响的。

    3.每次都产生新的workerid，用尽了怎么办？我的回答是很难，举个例子worker-id-bits的默认值是16位，也就是0-65535.相当于一个应用可以启动60000多次，一个应用的多个实例虽然共离这60000多次，但是这应该也够用了，而且它不支持自定义长度，可以把它的取值范围变的更长。

    4.如果真的有一天，workerid用尽了，也可以通过把数据库中对应的application_name的记录都清空，这样wokerid又是从0开始了

如果有问题邮箱联系：619089404@qq.com
