package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.entity.DataStore;
import com.miotech.kun.workflow.core.model.entity.HiveCluster;
import com.miotech.kun.workflow.core.model.entity.HiveTableStore;
import org.junit.Assert;
import org.junit.Test;

public class ClusterJsonUtilTest {


    @Test
    public void testDataStoreJson() throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("db", "tb", new HiveCluster(123L, "abc", "123", "sdf", "dsf", "sdf", "wer"));
        String json = DataStoreJsonUtil.toJson(dataStore);

        HiveTableStore hiveTableStore = (HiveTableStore) DataStoreJsonUtil.toDataStore(json);
        HiveCluster hiveCluster = (HiveCluster) hiveTableStore.getCluster();
        Assert.assertEquals("abc", hiveCluster.getDataStoreUrl());
    }

}
