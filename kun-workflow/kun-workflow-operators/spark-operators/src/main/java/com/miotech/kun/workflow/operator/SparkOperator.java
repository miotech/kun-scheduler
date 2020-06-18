package com.miotech.kun.workflow.operator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.ElasticSearchIndexStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.spark.models.SparkApp;
import com.miotech.kun.workflow.operator.spark.models.SparkJob;
import com.miotech.kun.workflow.operator.spark.models.StateInfo;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkOperator extends LivyBaseSparkOperator {

    private SparkApp app;

    @Override
    public boolean run(){
        OperatorContext context = getContext();
        logger.info("Start init spark job params");

        String jars = SparkConfiguration.getString(context, CONF_LIVY_BATCH_JARS);
        String files = SparkConfiguration.getString(context, CONF_LIVY_BATCH_FILES);
        String application = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_BATCH_APPLICATION);
        String args = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_BATCH_ARGS);
        String sparkConf = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_BATCH_CONF);

        // should using task name
        String sessionName = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_BATCH_NAME);
        if (Strings.isNullOrEmpty(sessionName)) {
            sessionName = "Spark Job: " + IdGenerator.getInstance().nextId();
        }

        SparkJob job = new SparkJob();
        if(!Strings.isNullOrEmpty(sessionName)){
            job.setName(sessionName);
        }
        if (!Strings.isNullOrEmpty(jars)) {
            job.setJars(Arrays.asList(jars.split(",")));
        }
        List<String> jobFiles = new ArrayList<>();
        if (!Strings.isNullOrEmpty(files)) {
            jobFiles = Arrays.stream(files.split(","))
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
        }
        if (!Strings.isNullOrEmpty(sparkConf)) {
            job.setConf( JSONUtils.jsonStringToStringMap(replaceWithVariable(sparkConf)));
        }

        if (!jobFiles.isEmpty()) {
            String mainEntry = jobFiles.get(0);
            boolean isJava;
            isJava = mainEntry.endsWith(".jar");
            job.setFile(mainEntry);
            logger.info("Find main entry file : {}", mainEntry);
            // set extra files
            List<String> extraFiles = jobFiles.size() > 1 ? jobFiles.subList(1, jobFiles.size()) : ImmutableList.of();
            if (!CollectionUtils.isEmpty(extraFiles))  {
                if (isJava) {
                    job.setFiles(extraFiles);
                } else {
                    job.setPyFiles(extraFiles);
                }
            }
        }
        if (!Strings.isNullOrEmpty(application)) {
            job.setClassName(application);
        }
        List<String> jobArgs = new ArrayList<>();
        if (!Strings.isNullOrEmpty(args)) {
            jobArgs = Arrays.stream(Arrays.stream(args.split("\\s+"))
                    .filter(x -> !x.isEmpty())
                    .map(String::trim).toArray(String[]::new))
                    .map(this::replaceWithVariable)
                    .collect(Collectors.toList());
        }

        if (!jobArgs.isEmpty()) {
            job.setArgs(jobArgs);
        }

        app = livyClient.runSparkJob(job);
        int jobId = app.getId();
        logger.info("Execute spark application using livy : batch id {}", jobId);
        logger.info("Execution job : {}", JSONUtils.toJsonString(job));

        StateInfo.State jobState;
        do {
            jobState = livyClient.getSparkJobState(app.getId()).getState();
            if (jobState == null) {
                throw new IllegalStateException("Cannot find state for job: " + app.getId());
            }
            waitFoSeconds(3);
        } while (!jobState.isFinished());

        logger.info("spark job \"{}\", batch id: {}" , jobState, jobId);
        if (jobState.equals(StateInfo.State.SUCCESS)) {
            // refresh app
            app = livyClient.getSparkJob(jobId);
            lineageAnalysis(context, app.getAppId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void abort() {
        logger.info("Delete spark batch job, id: " + app.getId());
        livyClient.deleteBatch(app.getId());
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, CONF_LIVY_PROXY_DEFAULT, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
                .define(CONF_LIVY_BATCH_JARS, ConfigDef.Type.STRING, true, "Java application jar files", CONF_LIVY_BATCH_JARS)
                .define(CONF_LIVY_BATCH_FILES, ConfigDef.Type.STRING, true, "files to use, seperated with `,`, the first file would be used as main entry", CONF_LIVY_BATCH_FILES)
                .define(CONF_LIVY_BATCH_APPLICATION, ConfigDef.Type.STRING, true, "application class name for java application", CONF_LIVY_BATCH_APPLICATION)
                .define(CONF_LIVY_BATCH_ARGS, ConfigDef.Type.STRING, true, "application arguments", CONF_LIVY_BATCH_ARGS)
                .define(CONF_LIVY_BATCH_CONF, ConfigDef.Type.STRING, true, "Extra spark configuration , in the format `{\"key\": \"value\"}`", CONF_LIVY_BATCH_CONF)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, true, "Spark arguments and configuration variables, use like `--param1 ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
                ;
    }

    public void lineageAnalysis(OperatorContext context, String applicationId){
        Preconditions.checkNotNull(applicationId);
        logger.info("Start lineage analysis for application id: " + applicationId);

        List<DataStore> inlets = new ArrayList<>();
        List<DataStore> outlets = new ArrayList<>();
        String dispatchAddress = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_DISPATCH_ADDRESS) + "/" + applicationId ;

        String input = dispatchAddress + ".input.txt";
        String output = dispatchAddress + ".output.txt";
        logger.info("Read lineage input {} and output {}", input, output);

        Configuration conf = new Configuration();
        String dispatchConfig = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_DISPATCH_CONFIG);
        if (!Strings.isNullOrEmpty(dispatchConfig)) {
            JSONUtils.jsonStringToStringMap(dispatchConfig)
                    .forEach(conf::set);
        }
        try {
            FileSystem fs = FileSystem.get(conf);
            try(BufferedReader inBufferReader = new BufferedReader(new InputStreamReader(fs.open(new Path(input))))) {
                inlets.addAll(genDataStore(inBufferReader));
            }
            try(BufferedReader outBufferReader = new BufferedReader(new InputStreamReader(fs.open(new Path(output))))) {
                outlets.addAll(genDataStore(outBufferReader));
            }
        } catch (IOException e){
            logger.error("lineage analysis from hdfs failed", e);
        }

        TaskAttemptReport taskAttemptReport = TaskAttemptReport.newBuilder()
                .withInlets(inlets)
                .withOutlets(outlets)
                .build();
        report(taskAttemptReport);
    }

    public List<DataStore> genDataStore(BufferedReader br) throws IOException {

        List<DataStore> stores = new ArrayList<>();

        String line;
        line = br.readLine();
        while (line != null) {
            String type = line.split("://")[0];
            switch (type){
                case "hdfs":
                    stores.add(getHiveStore(line));
                    break;
                case "mongodb":
                    stores.add(getMongoStore(line));
                    break;
                case "jdbc:postgresql":
                    stores.add(getPGStore(line));
                    break;
                case "elasticsearch":
                    stores.add(getESStore(line));
                    break;
                case "arango":
                    break;
                case "s3":
                    stores.add(getS3Store(line));
                    break;
                default:
                    logger.error(String.format("unknow resource type %s", type));
            }
            line = br.readLine();
        }

        return stores;
    }

    public ElasticSearchIndexStore getESStore(String line){
        String[] slices = line.split("/");
        int length = slices.length;
        String url = slices[length - 3];
        String index = slices[length - 2];
        return new ElasticSearchIndexStore(url,index);
    }

    public HiveTableStore getHiveStore(String line){
        String[] slices = line.split("/");
        int length = slices.length;
        String url = slices[2];
        String table = slices[length - 1];
        String db = slices[length - 2].split("\\.")[0];
        return new HiveTableStore(url, db, table);
    }

    public HiveTableStore getPGStore(String line){
        String[] slices = line.split(":");
        String table = slices[slices.length - 1];

        slices = line.split("/");
        String url = slices[2];
        String db = slices[3].split("\\?")[0];
        return new HiveTableStore(url, db, table);
    }

    public HiveTableStore getMongoStore(String line){
        String info = line.split("@")[1];
        String[] slices = info.split("/");
        String url = slices[0];
        String db = slices[1].split("\\.")[0];
        String table = slices[1].split("\\.")[1];
        return new HiveTableStore(url, db, table);
    }

    public HiveTableStore getS3Store(String line){
        String[] slices = line.split("/");
        int length = slices.length;
        String table = slices[length - 1];
        String db = slices[length - 2].toLowerCase();
        return new HiveTableStore(line, db, table);
    }

}
