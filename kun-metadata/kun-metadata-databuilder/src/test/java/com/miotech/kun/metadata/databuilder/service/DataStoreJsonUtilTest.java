package com.miotech.kun.metadata.databuilder.service;

import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class DataStoreJsonUtilTest {

    @Test
    public void testDataStoreJson_success() {
        DataStore dataStore = new HiveTableStore("url", "db", "tb");
        String json = DataStoreJsonUtil.toJson(dataStore);

        DataStore hiveDataStoreFromJson = DataStoreJsonUtil.toDataStore(json);
        MatcherAssert.assertThat(hiveDataStoreFromJson.getClass(), Matchers.theInstance(HiveTableStore.class));

        HiveTableStore hiveTableStore = (HiveTableStore) hiveDataStoreFromJson;
        String dataStoreUrl = hiveTableStore.getLocation();
        MatcherAssert.assertThat(dataStoreUrl, Matchers.is("url"));

        DataStore postgresDataStore = new PostgresDataStore("localhost", 5432, "pg_database", "pg_schema", "pg_table");
        String pgJson = DataStoreJsonUtil.toJson(postgresDataStore);

        DataStore pgDataStoreFromJson = DataStoreJsonUtil.toDataStore(pgJson);
        MatcherAssert.assertThat(pgDataStoreFromJson.getClass(), Matchers.theInstance(PostgresDataStore.class));

        PostgresDataStore pgDataStore = (PostgresDataStore) pgDataStoreFromJson;
        String database = pgDataStore.getDatabase();
        MatcherAssert.assertThat(database, Matchers.is("pg_database"));
    }

}
