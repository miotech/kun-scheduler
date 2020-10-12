package com.miotech.kun.workflow.operator;


import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.resolver.SparkSqlResolver;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class SparkSqlResolverTest {

    private SparkSqlResolver sparkSqlResolver = new SparkSqlResolver();

    @Test
    public void testInsertInto() {
        //insert into
        SparkSQLOperator sparkSQLOperator = new SparkSQLOperator();
        sparkSQLOperator.config();
        String sql = "insert into table a select B.b.name,C.c.id from B.b,C.c";
        String dbName = "default";
        String dataStoreUrl = "127.0.0.1:6886";
        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_SPARK_SQL, sql)
                .addConfig(SparkConfiguration.CONF_SPARK_DEFAULT_DB, dbName)
                .addConfig(SparkConfiguration.CONF_SPARK_DATASTORE_URL, dataStoreUrl)
                .build();
        HiveTableStore tableA = new HiveTableStore(dataStoreUrl, "default", "a");
        HiveTableStore tableB = new HiveTableStore(dataStoreUrl, "B", "b");
        HiveTableStore tableC = new HiveTableStore(dataStoreUrl, "C", "c");
        List<DataStore> downStream = sparkSqlResolver.resolveDownstreamDataStore(config);
        List<DataStore> upStream = sparkSqlResolver.resolveUpstreamDataStore(config);
        assertThat(downStream,containsInAnyOrder(tableA));
        assertThat(upStream,containsInAnyOrder(tableB,tableC));
    }

    @Test
    public void testSelectInto() {
        //select into(不支持，返回空list)
        SparkSQLOperator sparkSQLOperator = new SparkSQLOperator();
        sparkSQLOperator.config();
        String sql = "select b.id into c where b.age > 3";
        String dbName = "default";
        String dataStoreUrl = "127.0.0.1:6886";
        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_SPARK_SQL, sql)
                .addConfig(SparkConfiguration.CONF_SPARK_DEFAULT_DB, dbName)
                .addConfig(SparkConfiguration.CONF_SPARK_DATASTORE_URL, dataStoreUrl)
                .build();
        List<DataStore> downStream = sparkSqlResolver.resolveDownstreamDataStore(config);
        List<DataStore> upStream = sparkSqlResolver.resolveUpstreamDataStore(config);
        assertThat(downStream, hasSize(0));
        assertThat(upStream, hasSize(0));
    }

    @Test
    public void testCreateTable() {
        //create table
        SparkSQLOperator sparkSQLOperator = new SparkSQLOperator();
        sparkSQLOperator.config();
        String sql = "create table d as select b.id,c.name from b,c where b.id = c.id";
        String dbName = "default";
        String dataStoreUrl = "127.0.0.1:6886";
        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_SPARK_SQL, sql)
                .addConfig(SparkConfiguration.CONF_SPARK_DEFAULT_DB, dbName)
                .addConfig(SparkConfiguration.CONF_SPARK_DATASTORE_URL, dataStoreUrl)
                .build();
        HiveTableStore tableD = new HiveTableStore(dataStoreUrl, "default", "d");
        HiveTableStore tableB = new HiveTableStore(dataStoreUrl, "default", "b");
        HiveTableStore tableC = new HiveTableStore(dataStoreUrl, "default", "c");
        List<DataStore> downStream = sparkSqlResolver.resolveDownstreamDataStore(config);
        List<DataStore> upStream = sparkSqlResolver.resolveUpstreamDataStore(config);
        assertThat(downStream,containsInAnyOrder(tableD));
        assertThat(upStream,containsInAnyOrder(tableB,tableC));
    }

}
