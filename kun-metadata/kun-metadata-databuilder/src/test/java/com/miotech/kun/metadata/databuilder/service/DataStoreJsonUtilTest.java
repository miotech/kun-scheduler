package com.miotech.kun.metadata.databuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.metadata.databuilder.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class DataStoreJsonUtilTest {

    @Test
    public void testDataStoreJson_success() throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("url", "db", "tb");
        String json = DataStoreJsonUtil.toJson(dataStore);

        DataStore hiveDataStoreFromJson = DataStoreJsonUtil.toDataStore(json);
        MatcherAssert.assertThat(hiveDataStoreFromJson.getClass(), Matchers.theInstance(HiveTableStore.class));

        HiveTableStore hiveTableStore = (HiveTableStore) hiveDataStoreFromJson;
        String dataStoreUrl = hiveTableStore.getDataStoreUrl();
        MatcherAssert.assertThat(dataStoreUrl, Matchers.is("url"));

        DataStore postgresDataStore = new PostgresDataStore("jdbc:postgresql://localhost:5432/mdp", "pg_database", "pg_schema", "pg_table");
        String pgJson = DataStoreJsonUtil.toJson(postgresDataStore);

        DataStore pgDataStoreFromJson = DataStoreJsonUtil.toDataStore(pgJson);
        MatcherAssert.assertThat(pgDataStoreFromJson.getClass(), Matchers.theInstance(PostgresDataStore.class));

        PostgresDataStore pgDataStore = (PostgresDataStore) pgDataStoreFromJson;
        String database = pgDataStore.getDatabase();
        MatcherAssert.assertThat(database, Matchers.is("pg_database"));
    }

}
