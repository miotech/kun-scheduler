package com.miotech.kun.workflow.operator;


import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.resolver.SparkSqlResolver;

import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SparkSqlResolverTest {

    private SparkSqlResolver sparkSqlResolver = new SparkSqlResolver();

    @Test
    public void testInsertInto() {
        //insert into
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

    @Test
    public void testUDF(){
        String sql = "select test_udf(a)";
        List<SQLStatement> statements = SQLUtils.parseStatements(sql,"hive");
        DruidTestVisitor druidTestVisitor = new DruidTestVisitor();
        statements.get(0).accept(druidTestVisitor);
        Map<TableStat.Name, TableStat> tables = druidTestVisitor.getTables();
        List<String> result = new ArrayList<>();
        for(Map.Entry<TableStat.Name,TableStat> entry : tables.entrySet()){
            if(entry.getValue().getCreateCount() > 0){
                result.add(entry.getKey().toString());
            }
        }
        assertThat(result, containsInAnyOrder("a"));
    }

    @Test
    public void testCreateFromUnion() {
        String sql = "create table a as select * from C.c union select * from B.b";
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
    public void testCreateFromJoin() {
        String sql = "create table a as select * from C.c left join B.b";
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
    public void testInsertFromUnion() {
        String sql = "insert into table a select * from C.c union select * from B.b";
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
    public void testInsertFromJoin() {
        String sql = "insert into table a select * from C.c left join B.b";
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
    public void testInsertWithAlias() {
        String sql = "insert into table a select * from C.c as cc left join B.b as bb" +
                " on cc.id = bb.id";
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
    public void testInsertWithQuotation() {
        String sql = "INSERT INTO TABLE a select * FROM 'C'.c ,'B'.b ";
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
    public void testInsertCaseInsensitive() {
        String sql = "INSERT INTO TABLE a select * FROM C.c ,B.b ";
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


}
