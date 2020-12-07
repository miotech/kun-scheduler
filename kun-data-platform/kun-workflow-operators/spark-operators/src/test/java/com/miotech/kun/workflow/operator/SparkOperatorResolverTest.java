package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.operator.resolver.SparkOperatorResolver;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class SparkOperatorResolverTest extends GuiceTestBase {

    private SparkOperatorResolver sparkOperatorResolver;

    private HdfsFileSystem mockHdfsFileSystem;

    private final String HDFS_CONF = "{\"spark.hadoop.spline.hdfs_dispatcher.address\":\"hdfs://localhost:9000/tmp\"}";

    @Before
    public void init() {
        mockHdfsFileSystem = Mockito.mock(HdfsFileSystem.class);
        sparkOperatorResolver = new SparkOperatorResolver(mockHdfsFileSystem,1l);
    }


    @Test
    public void resolvePostgres() throws IOException {
        SplineSource up = SplineSource.newBuilder()
                .withSourceName("jdbc:postgresql://127.0.0.1:5432/kun:input")
                .withSourceType("jdbc")
                .build();
        SplineSource down = SplineSource.newBuilder()
                .withSourceName("jdbc:postgresql://127.0.0.1:5432/kun:output")
                .withSourceType("jdbc")
                .build();
        ExecPlan execPlan = ExecPlan.newBuilder()
                .withTaskName("pgExample")
                .withOutputSource(down)
                .withInputSources(Arrays.asList(up))
                .build();

        //write execPlan
        writeExecPlanToFile("/tmp/pgExample/1/123.execPlan.txt", execPlan);

        doReturn(getFilesInDir("/tmp/pgExample/1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());


        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_NAME, "pgExample")
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_CONF, HDFS_CONF)
                .build();
        List<DataStore> upstreamDataList = sparkOperatorResolver.resolveUpstreamDataStore(config);
        List<DataStore> downstreamDataList = sparkOperatorResolver.resolveDownstreamDataStore(config);
        PostgresDataStore upStore = (PostgresDataStore) upstreamDataList.get(0);
        PostgresDataStore downStore = (PostgresDataStore) downstreamDataList.get(0);
        assertThat(upstreamDataList, hasSize(1));
        assertThat(downstreamDataList, hasSize(1));
        assertThat(upStore.getDatabase(), is("kun"));
        assertThat(downStore.getDatabase(), is("kun"));
        assertThat(upStore.getTableName(), is("input"));
        assertThat(downStore.getTableName(), is("output"));
        assertThat(upStore.getUrl(), is("jdbc:postgresql://127.0.0.1:5432"));
        assertThat(downStore.getUrl(), is("jdbc:postgresql://127.0.0.1:5432"));
    }

    @Test
    public void resolveHive() throws IOException {
        SplineSource execPlan1_down = SplineSource.newBuilder()
                .withSourceName("file:/Users/shiki/spline-spark-agent/spark-warehouse/sparktest.db/hive_test")
                .withSourceType("hive")
                .build();
        ExecPlan execPlan1 = ExecPlan.newBuilder()
                .withTaskName("hive example1")
                .withOutputSource(execPlan1_down)
                .withInputSources(Arrays.asList())
                .build();

        doReturn(getFilesInDir("/tmp/hive example1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());
        writeExecPlanToFile("/tmp/hive example1/1/1.execPlan.txt", execPlan1);

        SplineSource execPlan2_up = SplineSource.newBuilder()
                .withSourceName("file:/Users/shiki/spline-spark-agent/spark-warehouse/sparktest.db/src")
                .withSourceType("hive")
                .build();
        SplineSource execPlan2_down = SplineSource.newBuilder()
                .withSourceName("file:/Users/shiki/spline-spark-agent/spark-warehouse/sparktest.db/hive_test")
                .withSourceType("hive")
                .build();
        ExecPlan execPlan2 = ExecPlan.newBuilder()
                .withTaskName("hive example1")
                .withOutputSource(execPlan2_down)
                .withInputSources(Arrays.asList(execPlan2_up))
                .build();

        //write execPlan2
        writeExecPlanToFile("/tmp/hive example1/1/2.execPlan.txt", execPlan2);
        doReturn(getFilesInDir("/tmp/hive example1/1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());

        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_NAME, "hive example1")
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_CONF, HDFS_CONF)
                .build();
        List<DataStore> upstreamDataList = sparkOperatorResolver.resolveUpstreamDataStore(config);
        List<DataStore> downstreamDataList = sparkOperatorResolver.resolveDownstreamDataStore(config);
        HiveTableStore upStore = (HiveTableStore) upstreamDataList.get(0);
        HiveTableStore downStore = (HiveTableStore) downstreamDataList.get(0);
        assertThat(upstreamDataList, hasSize(1));
        assertThat(downstreamDataList, hasSize(1));
        assertThat(upStore.getDatabase(), is("sparktest"));
        assertThat(downStore.getDatabase(), is("sparktest"));
        assertThat(upStore.getTable(), is("src"));
        assertThat(downStore.getTable(), is("hive_test"));
        assertThat(upStore.getDataStoreUrl(), is("file:/Users/shiki/spline-spark-agent/spark-warehouse"));
        assertThat(downStore.getDataStoreUrl(), is("file:/Users/shiki/spline-spark-agent/spark-warehouse"));
    }

    @Test
    public void resolveHiveJdbc() throws IOException {

        SplineSource up = SplineSource.newBuilder()
                .withSourceName("jdbc:hive2://127.0.0.1:10000/sparktest:input")
                .withSourceType("jdbc")
                .build();
        SplineSource execPlan1_down = SplineSource.newBuilder()
                .withSourceName("jdbc:hive2://127.0.0.1:10000/sparktest:output")
                .withSourceType("jdbc")
                .build();
        ExecPlan execPlan1 = ExecPlan.newBuilder()
                .withTaskName("hiveExample2")
                .withOutputSource(execPlan1_down)
                .withInputSources(Arrays.asList(up))
                .build();

        //write execPlan
        writeExecPlanToFile("/tmp/hiveExample2/1/1.execPlan.txt", execPlan1);
        doReturn(getFilesInDir("/tmp/hiveExample2/1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());
        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_NAME, "hiveExample2")
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_CONF, HDFS_CONF)
                .build();
        List<DataStore> upstreamDataList = sparkOperatorResolver.resolveUpstreamDataStore(config);
        List<DataStore> downstreamDataList = sparkOperatorResolver.resolveDownstreamDataStore(config);
        HiveTableStore upStore = (HiveTableStore) upstreamDataList.get(0);
        HiveTableStore downStore = (HiveTableStore) downstreamDataList.get(0);
        assertThat(upstreamDataList, hasSize(1));
        assertThat(downstreamDataList, hasSize(1));
        assertThat(upStore.getDatabase(), is("sparktest"));
        assertThat(downStore.getDatabase(), is("sparktest"));
        assertThat(upStore.getTable(), is("input"));
        assertThat(downStore.getTable(), is("output"));
        assertThat(upStore.getDataStoreUrl(), is("jdbc:hive2://127.0.0.1:10000"));
        assertThat(downStore.getDataStoreUrl(), is("jdbc:hive2://127.0.0.1:10000"));
    }

    @Test
    public void resolveMongo() throws IOException {
        SplineSource up = SplineSource.newBuilder()
                .withSourceName("mongodb://admin:123456@127.0.0.1:27017/spline.input")
                .withSourceType("mongodb")
                .build();
        SplineSource down = SplineSource.newBuilder()
                .withSourceName("mongodb://admin:123456@127.0.0.1:27017/spline.output")
                .withSourceType("mongodb")
                .build();
        ExecPlan execPlan = ExecPlan.newBuilder()
                .withTaskName("mongoExample")
                .withOutputSource(down)
                .withInputSources(Arrays.asList(up))
                .build();

        //write execPlan
        writeExecPlanToFile("/tmp/mongoExample/1/1.execPlan.txt", execPlan);
        doReturn(getFilesInDir("/tmp/mongoExample/1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());

        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_NAME, "mongoExample")
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_CONF, HDFS_CONF)
                .build();
        List<DataStore> upstreamDataList = sparkOperatorResolver.resolveUpstreamDataStore(config);
        List<DataStore> downstreamDataList = sparkOperatorResolver.resolveDownstreamDataStore(config);
        MongoDataStore upStore = (MongoDataStore) upstreamDataList.get(0);
        MongoDataStore downStore = (MongoDataStore) downstreamDataList.get(0);
        assertThat(upstreamDataList, hasSize(1));
        assertThat(downstreamDataList, hasSize(1));
        assertThat(upStore.getDatabase(), is("spline"));
        assertThat(downStore.getDatabase(), is("spline"));
        assertThat(upStore.getCollection(), is("input"));
        assertThat(downStore.getCollection(), is("output"));
        assertThat(upStore.getUrl(), is("mongodb://127.0.0.1:27017"));
        assertThat(downStore.getUrl(), is("mongodb://127.0.0.1:27017"));
    }

    @Test
    public void resolveEs() throws IOException {
        SplineSource up = SplineSource.newBuilder()
                .withSourceName("jdbc:postgresql://127.0.0.1:5432/spline:input")
                .withSourceType("jdbc")
                .build();
        SplineSource down = SplineSource.newBuilder()
                .withSourceName("elasticsearch://localhost/test_es_spline/blog")
                .withSourceType("elasticsearch")
                .build();
        ExecPlan execPlan = ExecPlan.newBuilder()
                .withTaskName("esExample")
                .withOutputSource(down)
                .withInputSources(Arrays.asList(up))
                .build();

        //write execPlan
        writeExecPlanToFile("/tmp/esExample/1/1.execPlan.txt", execPlan);
        doReturn(getFilesInDir("/tmp/esExample/1")).when(mockHdfsFileSystem).copyFilesInDir(Mockito.any());
        doNothing().when(mockHdfsFileSystem).deleteFilesInDir(Mockito.any());

        Config config = Config.newBuilder()
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_NAME, "esExample")
                .addConfig(SparkConfiguration.CONF_LIVY_BATCH_CONF, HDFS_CONF)
                .build();

        List<DataStore> upstreamDataList = sparkOperatorResolver.resolveUpstreamDataStore(config);
        List<DataStore> downstreamDataList = sparkOperatorResolver.resolveDownstreamDataStore(config);
        PostgresDataStore upStore = (PostgresDataStore) upstreamDataList.get(0);
        ElasticSearchIndexStore downStore = (ElasticSearchIndexStore) downstreamDataList.get(0);
        assertThat(upstreamDataList, hasSize(1));
        assertThat(downstreamDataList, hasSize(1));
        assertThat(upStore.getDatabase(), is("spline"));
        assertThat(upStore.getTableName(), is("input"));
        assertThat(downStore.getIndex(), is("test_es_spline"));
        assertThat(upStore.getUrl(), is("jdbc:postgresql://127.0.0.1:5432"));
        assertThat(downStore.getDataStoreUrl(), is("elasticsearch://localhost"));
    }


    private void writeExecPlanToFile(String fileName, ExecPlan execPlan) throws IOException {
        String dir = fileName.substring(0, fileName.lastIndexOf('/'));
        File folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(fileName);
        writer.write(JSONUtils.toJsonString(execPlan));
        writer.close();
    }

    private List<String> getFilesInDir(String dir) throws IOException {
        File file = new File(dir);
        List<String> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            return Arrays.stream(file.listFiles()).map(x -> x.getPath()).collect(Collectors.toList());
        }
        return fileList;
    }

    private void deleteFilesInDir(String dir) throws IOException {
        File file = new File(dir);
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        }
    }

}
