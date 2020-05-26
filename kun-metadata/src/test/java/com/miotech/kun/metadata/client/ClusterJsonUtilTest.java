package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.junit.Assert;
import org.junit.Test;

public class ClusterJsonUtilTest {

    @Test
    public void testDataStoreJson() throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("url", "db", "tb");
        String json = DataStoreJsonUtil.toJson(dataStore);

        HiveTableStore hiveTableStore = (HiveTableStore) DataStoreJsonUtil.toDataStore(json);
        String dataStoreUrl = hiveTableStore.getDataStoreUrl();
        Assert.assertEquals("url", dataStoreUrl);
    }

}
