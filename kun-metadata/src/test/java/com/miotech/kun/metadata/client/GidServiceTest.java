package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.metadata.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GidServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Before
    public void createTable() {
        operator.update("CREATE TABLE `kun_mt_dataset_gid` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `data_store` varchar(1024) DEFAULT NULL,\n" +
                "  `dataset_gid` bigint(20) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");
    }

    @Test
    public void testGenerate_non_exist() {
        DataStore dataStore = new HiveTableStore("", "db1", "tb");
        GidService generator = new GidService(operator);
        long generate = generator.generate(dataStore);
        Assert.assertNotNull(generate);
    }

    @Test
    public void testGenerate_existed() throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("", "db1", "tb");

        long currentTime = System.currentTimeMillis();
        String dataStoreJson = DataStoreJsonUtil.toJson(dataStore);
        operator.update("INSERT INTO kun_mt_dataset_gid(data_store, dataset_gid) VALUES (?, ?)", dataStoreJson, currentTime);

        GidService generator = new GidService(operator);
        long generate = generator.generate(dataStore);
        Assert.assertEquals(currentTime, generate);
    }

}
