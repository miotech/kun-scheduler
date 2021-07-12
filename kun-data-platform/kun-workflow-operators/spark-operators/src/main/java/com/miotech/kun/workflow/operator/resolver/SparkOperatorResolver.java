package com.miotech.kun.workflow.operator.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.operator.HdfsFileSystem;
import com.miotech.kun.workflow.operator.SparkConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SparkOperatorResolver implements Resolver {

    private static final Logger logger = LoggerFactory.getLogger(SparkOperatorResolver.class);

    private final ObjectMapper MAPPER = new ObjectMapper();

    private HdfsFileSystem hdfsFileSystem;

    private final Long taskRunId;

    private Map<Long, Pair<List<DataStore>, List<DataStore>>> resolvedTask = new HashMap<>();


    private final String JDBC_FORMAT = "jdbc:(.*)://(.*)/(.*):(.*)";
    private final String MONGO_FORMAT = "mongodb://(.*)/(.*)\\.(.*)";
    private final String ES_FORMAT = "elasticsearch://(.*)/(.*)";
    private final String HIVE_FORMAT = "(.*)/(.*)/(.*)";

    public SparkOperatorResolver(HdfsFileSystem hdfsFileSystem, Long taskRunId) {
        this.hdfsFileSystem = hdfsFileSystem;
        this.taskRunId = taskRunId;
    }


    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        if (resolvedTask.containsKey(taskRunId)) {
            return resolvedTask.get(taskRunId).getLeft();
        }
        logger.debug("resolve upstream data store for taskRun = {}", taskRunId);
        List<ExecPlan> execPlanList = getExecPlanByConfig(config);
        return analyzeTaskExecPlan(execPlanList).getLeft();
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        if (resolvedTask.containsKey(taskRunId)) {
            return resolvedTask.get(taskRunId).getRight();
        }
        logger.debug("resolve downstream data store for task = {}", taskRunId);
        List<ExecPlan> execPlanList = getExecPlanByConfig(config);
        return analyzeTaskExecPlan(execPlanList).getRight();
    }


    //根据taskRunI获取对应的执行计划
    private List<ExecPlan> getExecPlanByConfig(Config config) {
        String sparkConf = config.getString(SparkConfiguration.CONF_LIVY_BATCH_CONF);
        logger.debug("spark conf = {}", sparkConf);
        String dirAddress = "lineage/" + taskRunId;
        logger.debug("read lineage dir = {}", dirAddress);
        List<String> files = new ArrayList<>();
        try {
            files = hdfsFileSystem.copyFilesInDir(dirAddress);
            hdfsFileSystem.deleteFilesInDir(dirAddress);
        } catch (IOException e) {
            logger.error("lineage analysis from hdfs failed", e);
        }
        return filesToExecPlan(files);
    }


    //解析任务的执行计划，生成数据上下游
    private Pair<List<DataStore>, List<DataStore>> analyzeTaskExecPlan(List<ExecPlan> execPlanList) {
        //任务上游数据源
        Map<String, SplineSource> upstream = new HashMap<>();
        //任务下游数据源
        Map<String, SplineSource> downStream = new HashMap<>();
        for (ExecPlan execPlan : execPlanList) {
            List<SplineSource> inputSources = execPlan.getInputSources();
            if (inputSources != null) {
                for (SplineSource splineSource : inputSources) {
                    //若执行计划的上游数据源是其他执行计划的下游，则将该数据源从任务下游数据源中删除
                    if (!downStream.containsKey(splineSource.getSourceName())) {
                        upstream.put(splineSource.getSourceName(), splineSource);
                    }
                }
            }
            SplineSource outputSource = execPlan.getOutputSource();
            if (upstream.containsKey(outputSource.getSourceName())) {
                upstream.remove(outputSource.getSourceName());
            }
            downStream.put(outputSource.getSourceName(), outputSource);
        }
        List<DataStore> upstreamDataStore = new ArrayList<>();
        for (SplineSource splineSource : upstream.values()) {
            try {
                upstreamDataStore.add(dataSourcesToDataStore(splineSource.getSourceName(), splineSource.getSourceType()));
            } catch (IllegalStateException e) {
                logger.warn("could not cast dataSource = {} to dataStore", splineSource);
            }
        }
        List<DataStore> downStreamDataStore = new ArrayList<>();
        for (SplineSource splineSource : downStream.values()) {
            try {
                downStreamDataStore.add(dataSourcesToDataStore(splineSource.getSourceName(), splineSource.getSourceType()));
            } catch (IllegalStateException e) {
                logger.warn("could not cast dataSource = {} to dataStore", splineSource);
            }
        }
        //缓存解析结果
        Pair<List<DataStore>, List<DataStore>> result = Pair.of(upstreamDataStore, downStreamDataStore);
        resolvedTask.put(taskRunId, result);
        return result;


    }

    private List<ExecPlan> filesToExecPlan(List<String> files) {
        Collections.sort(files);
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
                            dataStore = toPostgres(matcher.group(2),
                                    matcher.group(3), matcher.group(4));
                            break;
                        case "hive2":
                            dataStore = new HiveTableStore("jdbc:" + dataType + "://" + matcher.group(2),
                                    matcher.group(3).toLowerCase(), matcher.group(4));
                            break;

                        default:
                            logger.error("unknown datasource type {}", dataType);
                            throw new IllegalStateException("Invalid datasource type : " + dataType);
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
            return new HiveTableStore(datasource, database.toLowerCase(), table.toLowerCase());
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
            String[] hostAndPort = host.split(":");
            return new ElasticSearchIndexStore(hostAndPort[0], Integer.parseInt(hostAndPort[1]), index);
        } else {
            logger.error("Illegal es datasource {}", datasource);
            throw new IllegalStateException("Illegal es datasource : " + datasource);
        }
    }

    private ArangoCollectionStore toArango(String datasource) {
        return null;
    }

    private PostgresDataStore toPostgres(String url, String database, String tableName) {
        String schema = "public";
        if (tableName.contains(".")) {
            String[] strs = tableName.split("\\.");
            schema = strs[0];
            tableName = strs[1];
        }

        String[] hostAndPort = url.split(":");
        return new PostgresDataStore(hostAndPort[0], Integer.parseInt(hostAndPort[1]), database, schema, tableName);
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

            String[] hostAndPort = connect.split(":");
            return new MongoDataStore(hostAndPort[0], Integer.parseInt(hostAndPort[1]), database, collection);

        } else {
            logger.error("Illegal mongo datasource {}", datasource);
            throw new IllegalStateException("Illegal mongo datasource : " + datasource);
        }
    }
}
