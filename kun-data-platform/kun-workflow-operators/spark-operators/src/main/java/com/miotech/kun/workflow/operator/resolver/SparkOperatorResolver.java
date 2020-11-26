package com.miotech.kun.workflow.operator.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.operator.HdfsFileSystem;
import com.miotech.kun.workflow.operator.SparkConfiguration;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SparkOperatorResolver implements Resolver {

    private static final Logger logger = LoggerFactory.getLogger(SparkOperatorResolver.class);

    private final ObjectMapper MAPPER = new ObjectMapper();

    private HdfsFileSystem hdfsFileSystem;

    private Map<String, Pair<List<DataStore>, List<DataStore>>> resolvedTask = new HashMap<>();


    private final String HDFS_DEFAULT_FS = "fs.defaultFS";
    private final String SPLINE_HDFS_ADDRESS = "spline.hdfs_dispatcher.address";
    private final String JDBC_FORMAT = "jdbc:(.*)://(.*)/(.*):(.*)";
    private final String MONGO_FORMAT = "mongodb://(.*)/(.*)\\.(.*)";
    private final String ES_FORMAT = "elasticsearch://(.*)/(.*)";
    private final String HIVE_FORMAT = "(.*)/(.*)/(.*)";

    public SparkOperatorResolver(HdfsFileSystem hdfsFileSystem){
        this.hdfsFileSystem = hdfsFileSystem;
    }


    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        String taskName = config.getString(SparkConfiguration.CONF_LIVY_BATCH_NAME);
        if (resolvedTask.containsKey(taskName)) {
            return resolvedTask.get(taskName).getLeft();
        }
        logger.debug("resolve upstream data store for task = {}", taskName);
        List<ExecPlan> execPlanList = getExecPlanByConfig(config);
        return analyzeTaskExecPlan(execPlanList, taskName).getLeft();
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        String taskName = config.getString(SparkConfiguration.CONF_LIVY_BATCH_NAME);
        if (resolvedTask.containsKey(taskName)) {
            return resolvedTask.get(taskName).getRight();
        }
        logger.debug("resolve downstream data store for task = {}", taskName);
        List<ExecPlan> execPlanList = getExecPlanByConfig(config);
        return analyzeTaskExecPlan(execPlanList, taskName).getRight();
    }


    //根据taskName（与提交到spark的应用名一致）获取对应的执行计划
    private List<ExecPlan> getExecPlanByConfig(Config config) {
        String taskName = config.getString(SparkConfiguration.CONF_LIVY_BATCH_NAME);
        String sparkConf = config.getString(SparkConfiguration.CONF_LIVY_BATCH_CONF);
        logger.debug("spark conf = {}", sparkConf);
        String hdfsDir = JSONUtils.jsonStringToMap(sparkConf).get(SPLINE_HDFS_ADDRESS).toString();
        String dirAddress = hdfsDir.substring(hdfsDir.lastIndexOf("/") + 1) + "/" + taskName;
        logger.debug("read lineage dir = {}", dirAddress);
        List<String> files = new ArrayList<>();
        try {
            files = hdfsFileSystem.copyFilesInDir(dirAddress);
            hdfsFileSystem.deleteFilesInDir(dirAddress);
        } catch (IOException e) {
            logger.error("lineage analysis from hdfs failed", e);
        }
        return filesToExecPlan(taskName, files);
    }


    //解析任务的执行计划，生成数据上下游
    private Pair<List<DataStore>, List<DataStore>> analyzeTaskExecPlan(List<ExecPlan> execPlanList, String taskName) {
        //任务上游数据源
        Map<String, SplineSource> upstream = new HashMap<>();
        //任务下游数据源
        Map<String, SplineSource> downStream = new HashMap<>();
        for (ExecPlan execPlan : execPlanList) {
            List<SplineSource> inputSources = execPlan.getInputSources();
            for (SplineSource splineSource : inputSources) {
                //若执行计划的上游数据源是其他执行计划的下游，则将该数据源从任务上游数据源中删除
                if (downStream.containsKey(splineSource.getSourceName())) {
                    upstream.remove(splineSource.getSourceName());
                } else {
                    upstream.put(splineSource.getSourceName(), splineSource);
                }
            }
            SplineSource outputSource = execPlan.getOutputSource();
            if (upstream.containsKey(outputSource.getSourceName())) {
                downStream.remove(outputSource.getSourceName());
            } else {
                downStream.put(outputSource.getSourceName(), outputSource);
            }
        }
        List<DataStore> upstreamDataStore = upstream.values().stream()
                .map(x -> dataSourcesToDataStore(x.getSourceName(), x.getSourceType()))
                .collect(Collectors.toList());
        List<DataStore> downStreamDataStore = downStream.values().stream()
                .map(x -> dataSourcesToDataStore(x.getSourceName(), x.getSourceType()))
                .collect(Collectors.toList());
        //缓存解析结果
        Pair<List<DataStore>, List<DataStore>> result = Pair.of(upstreamDataStore, downStreamDataStore);
        resolvedTask.put(taskName, result);
        return result;


    }

    private List<ExecPlan> filesToExecPlan(String taskName, List<String> files) {
        List<ExecPlan> execPlanList = new ArrayList<>();
        for (String fileName : files) {
            try {
                execPlanList.add(MAPPER.readValue(new File(fileName), ExecPlan.class));
            } catch (IOException e) {
                logger.error("failed to get ExecPlan from file = {}", fileName, e);
            }
        }
        return execPlanList;
    }


    //将datasource转换成对应的DataStore
    private DataStore dataSourcesToDataStore(String datasource, String type) {
        DataStore dataStore;
        switch (type) {
            case "hive":
            case "parquet":
                dataStore = toHive(datasource);
                break;
            case "mongodb":
                dataStore = toMongo(datasource);
                break;
            case "elasticsearch":
                dataStore = toES(datasource);
                break;
            case "jdbc":
                Pattern pattern = Pattern.compile(JDBC_FORMAT);
                Matcher matcher = pattern.matcher(datasource);
                if (matcher.matches()) {
                    String dataType = matcher.group(1);
                    switch (dataType) {
                        case "postgresql":
                            dataStore = toPostgres("jdbc:" + dataType + "://" + matcher.group(2),
                                    matcher.group(3), matcher.group(4));
                            break;
                        case "hive2":
                            dataStore = new HiveTableStore("jdbc:" + dataType + "://" + matcher.group(2),
                                    matcher.group(3),matcher.group(4));
                            break;

                        default:
                            logger.error("unknown datasource type {}", type);
                            throw new IllegalStateException("Invalid datasource type : " + type);
                    }
                } else {
                    logger.error("unknown datasource type {}", type);
                    throw new IllegalStateException("Invalid datasource type : " + type);
                }
                break;
            default:
                logger.error("unknown datasource type {}", type);
                throw new IllegalStateException("Invalid datasource type : " + type);

        }
        return dataStore;
    }

    private HiveTableStore toHive(String datasource) {
        Pattern pattern = Pattern.compile(HIVE_FORMAT);
        Matcher matcher = pattern.matcher(datasource);
        if (matcher.matches()) {
            String table = matcher.group(3);
            String database = matcher.group(2);
            if (database.contains(".")) {
                database = database.split("\\.")[0];
            } else {
                database = "default";
            }
            return new HiveTableStore(matcher.group(1), database, table);
        } else {
            logger.error("Illegal hive datasource {}", datasource);
            throw new IllegalStateException("Illegal hive datasource : " + datasource);
        }
    }

    private ElasticSearchIndexStore toES(String datasource) {
        Pattern pattern = Pattern.compile(ES_FORMAT);
        Matcher matcher = pattern.matcher(datasource);
        if (matcher.matches()) {
            String index = matcher.group(2);
            String host = matcher.group(1);
            if (host.contains("/")) {
                String strs[] = host.split("/");
                host = strs[0];
                index = strs[1];
            }
            return new ElasticSearchIndexStore("elasticsearch://" + host, index);
        } else {
            logger.error("Illegal es datasource {}", datasource);
            throw new IllegalStateException("Illegal es datasource : " + datasource);
        }
    }

    private ArangoCollectionStore toArango(String datasource) {
        return null;
    }

    private PostgresDataStore toPostgres(String url, String database, String tableName) {
        return new PostgresDataStore(url, database, "", tableName);
    }

    private MongoDataStore toMongo(String datasource) {
        Pattern pattern = Pattern.compile(MONGO_FORMAT);
        Matcher matcher = pattern.matcher(datasource);
        if (matcher.matches()) {
            String connect = matcher.group(1);
            String database = matcher.group(2);
            String collection = matcher.group(3);
            if (connect.contains("@")) {
                int index = connect.indexOf('@');
                String auth = connect.substring(0, index + 1);
                connect = connect.replace(auth, "");
            }
            return new MongoDataStore("mongodb://" + connect, database, collection);

        } else {
            logger.error("Illegal mongo datasource {}", datasource);
            throw new IllegalStateException("Illegal mongo datasource : " + datasource);
        }
    }
}
