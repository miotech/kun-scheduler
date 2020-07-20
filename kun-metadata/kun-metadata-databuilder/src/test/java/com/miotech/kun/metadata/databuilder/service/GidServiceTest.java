package com.miotech.kun.metadata.databuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.service.gid.DataStoreJsonUtil;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.commons.db.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

public class GidServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Inject
    private GidService gidService;

    @Test
    public void testGenerate_non_exist() {
        DataStore dataStore = new HiveTableStore("", "db1", "tb");
        long generate = gidService.generate(dataStore);
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
