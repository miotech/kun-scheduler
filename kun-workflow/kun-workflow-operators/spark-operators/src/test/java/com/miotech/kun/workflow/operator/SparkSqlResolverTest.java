package com.miotech.kun.workflow.operator;


import static org.junit.Assert.assertEquals;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.resolver.SparkSqlResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SparkSqlResolverTest {

    private SparkSqlResolver sparkSqlResolver = new SparkSqlResolver();

    @Test
    public void testSparkSqlParse() {
        //insert into
        String sql = "insert into table a select b.name,c.id from b,c";
        List<String> downStream = sparkSqlResolver.getDownStream(sql, "hive");
        List<String> upStream = sparkSqlResolver.getUpStream(sql, "hive");
        assertEquals(Arrays.asList("a"), downStream);
        assertEquals(Arrays.asList("b", "c"), upStream);
        //select into(不支持，返回空list)
        sql = "select b.id into c where b.age > 3";
        downStream = sparkSqlResolver.getDownStream(sql, "hive");
        upStream = sparkSqlResolver.getUpStream(sql, "hive");
        assertEquals(Arrays.asList(), downStream);
        assertEquals(Arrays.asList(), upStream);
        //create table
        sql = "create table d as select b.id,c.name from b,c where a.id = b.id";
        downStream = sparkSqlResolver.getDownStream(sql, "hive");
        upStream = sparkSqlResolver.getUpStream(sql, "hive");
        assertEquals(Arrays.asList("d"), downStream);
        assertEquals(Arrays.asList("b", "c"), upStream);
    }

    @Test
    public void testResolveDataStore() {
        SparkSQLOperator sparkSQLOperator = new SparkSQLOperator();
        sparkSQLOperator.config();
        String sql = "insert into table a select B.b.name,C.c.id from B.b,C.c";
        String dbName = "default";
        String dataStoreUrl = "127.0.0.1:6886";
        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_SPARK_SQL,sql)
                .addConfig(SparkConfiguration.CONF_SPARK_DEFAULT_DB,dbName)
                .addConfig(SparkConfiguration.CONF_SPARK_DATASTORE_URL,dataStoreUrl)
                .build();
        HiveTableStore tableA = new HiveTableStore(dataStoreUrl,"default","a");
        HiveTableStore tableB = new HiveTableStore(dataStoreUrl,"B","b");
        HiveTableStore tableC = new HiveTableStore(dataStoreUrl,"C","c");
        List<DataStore> downStream = sparkSqlResolver.resolveDownstreamDataStore(config);
        List<DataStore> upStream = sparkSqlResolver.resolveUpstreamDataStore(config);
        assertEquals(Arrays.asList(tableA),downStream);
        assertEquals(Arrays.asList(tableB,tableC),upStream);
    }
}
