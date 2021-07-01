package com.miotech.kun.metadata.databuilder.model;

import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.model.lineage.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DataStoreTest {

    private static final String URL = "test_url";
    private static final String HOST = "host";
    private static final int PORT = 7890;
    private static final String DATABASE = "test_database";
    private static final String SCHEMA = "test_schema";
    private static final String TABLE = "test_table";

    @Test
    public void testGetDatabaseName_arango() {
        DataStore dataStore = new ArangoCollectionStore(HOST, PORT, DATABASE, TABLE);
        assertThat(dataStore.getDatabaseName(), is(DATABASE));
    }

    @Test
    public void testGetDatabaseName_es() {
        DataStore dataStore = new ElasticSearchIndexStore(HOST, PORT, TABLE);
        assertThat(dataStore.getDatabaseName(), nullValue());
    }

    @Test
    public void testGetDatabaseName_hive() {
        DataStore dataStore = new HiveTableStore(URL, DATABASE, TABLE);
        assertThat(dataStore.getDatabaseName(), is(DATABASE));
    }

    @Test
    public void testGetDatabaseName_mongo() {
        DataStore dataStore = new MongoDataStore(HOST, PORT, DATABASE, TABLE);
        assertThat(dataStore.getDatabaseName(), is(DATABASE));
    }

    @Test
    public void testGetDatabaseName_postgres() {
        DataStore dataStore = new PostgresDataStore(HOST, PORT, DATABASE, SCHEMA, TABLE);
        assertThat(dataStore.getDatabaseName(), is(String.format("%s.%s", DATABASE, SCHEMA)));
    }


}
