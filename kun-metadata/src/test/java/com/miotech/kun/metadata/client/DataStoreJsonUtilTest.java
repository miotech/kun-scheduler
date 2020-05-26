package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.junit.Assert;
import org.junit.Test;

public class DataStoreJsonUtilTest {

    @Test
    public void testDataStoreJson() throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("url", "db", "tb");
        String json = DataStoreJsonUtil.toJson(dataStore);

        HiveTableStore hiveTableStore = (HiveTableStore) DataStoreJsonUtil.toDataStore(json);
        String dataStoreUrl = hiveTableStore.getDataStoreUrl();
        Assert.assertEquals("url", dataStoreUrl);

        DataStore postgresDataStore = new PostgresDataStore("jdbc:postgresql://192.168.1.211:5432/mdp", "pg_database", "pg_schema", "pg_table");
        String pgJson = DataStoreJsonUtil.toJson(postgresDataStore);
        PostgresDataStore pgDataStore = (PostgresDataStore) DataStoreJsonUtil.toDataStore(pgJson);
        String database = pgDataStore.getDatabase();
        Assert.assertEquals("pg_database", database);
    }

}
