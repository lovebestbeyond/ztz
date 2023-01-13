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