package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.entrance.Entrance;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

public class EntranceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Before
    public void init() {
        operator.update("CREATE TABLE `kun_mt_cluster` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `type` varchar(1024) DEFAULT NULL,\n" +
                "  `url` varchar(1024) DEFAULT NULL,\n" +
                "  `username` varchar(1024) DEFAULT NULL,\n" +
                "  `password` varchar(1024) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");

        operator.update("INSERT INTO `kun_mt_cluster`(`type`, `url`, `username`, `password`) VALUES ('postgres', 'jdbc:postgresql://192.168.1.62:5432/si-dump', 'postgres', 'Mi0ying2017')");
        operator.update("INSERT INTO `kun_mt_cluster`(`type`, `url`, `username`, `password`) VALUES ('hive', 'jdbc:hive2://10.0.0.85:10000;jdbc:mysql://10.0.0.85:13306/hive', 'hive;miotech', ';Mi0Tech@2018')");
    }

    @Test
    public void testStart() {
        Entrance entrance = new Entrance(operator, new PrintLoader());
        entrance.start();
    }

    @Test
    public void testStart_id() {
        Entrance entrance = new Entrance(operator, new PrintLoader());
        entrance.start(1L);
    }

}
