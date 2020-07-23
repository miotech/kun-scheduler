package com.miotech.kun.workflow.operator;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.ElasticSearchIndexStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.model.clients.LivyClient;
import com.miotech.kun.workflow.operator.model.models.SparkApp;
import com.miotech.kun.workflow.operator.model.models.SparkJob;
import com.miotech.kun.workflow.utils.JSONUtils;
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

public class SparkOperator extends KunOperator {

    SparkJob job = new SparkJob();
    Logger logger;
    SparkApp app;
    LivyClient livyClient;

    @Override
    public void init(){
        OperatorContext context = getContext();

        logger = context.getLogger();
        logger.info("Start init spark job params");

        String jars = context.getConfig().getString("jars");
        String files = context.getConfig().getString("files");
        String application = context.getConfig().getString("application");
        String args = context.getConfig().getString("args");

        String sessionName = context.getConfig().getString("name");
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

        if (!jobFiles.isEmpty()) {
            String mainEntry = jobFiles.get(0);
            boolean isJava;
            isJava = mainEntry.endsWith(".jar");
            job.setFile(mainEntry);
            logger.info("Find main entry file : {}", mainEntry);
            // set extra files
            List<String> extraFiles = jobFiles.size() > 1 ? jobFiles.subList(1, jobFiles.size()-1) : null;
            if (isJava)  {
                job.setFiles(extraFiles);
            } else {
                job.setPyFiles(extraFiles);
            }
        }
        if (!Strings.isNullOrEmpty(application)) {
            job.setClassName(application);
        }
        List<String> jobArgs = new ArrayList<>();
        if (!Strings.isNullOrEmpty(args)) {
            List<String> trimArgs = Arrays.stream(args.split("\\s+"))
                    .filter(x -> !x.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            jobArgs = Arrays.stream(trimArgs.toArray(new String[0]))
                    .map(x -> x.startsWith("$") ? context.getConfig().getString(x.substring(1)) : x)
                    .collect(Collectors.toList());
        }

        if (!jobArgs.isEmpty()) {
            job.setArgs(jobArgs);
        }

        String livyHost = context.getConfig().getString("livyHost");
        String queue = context.getConfig().getString("queue", "default");
        String proxyUser = context.getConfig().getString("proxyUser", "hadoop");
        livyClient = new LivyClient(livyHost, queue, proxyUser);
    }

    @Override
    public boolean run(){
        OperatorContext context = getContext();

        try {
            app = livyClient.runSparkJob(job);
            int jobId = app.getId();
            logger.info("Execute spark application using livy : batch id {}", app.getId());
            logger.info("Execution job : {}", JSONUtils.toJsonString(job));
            while(true) {
                String state = app.getState();
                if(state == null){
                    // job might be deleted
                    logger.debug("cannot get spark job state, batch id: " + app.getId());
                    return false;
                }
                switch (state) {
                    case "not_started":
                    case "starting":
                    case "busy":
                    case "idle":
                    case "running":
                        logger.debug("spark job running, batch id: " + app.getId());
                        app = livyClient.getSparkJob(jobId);
                        Thread.sleep(10000);
                        break;
                    case "shutting_down":
                    case "killed":
                    case "dead":
                    case "error":
                        logger.info("spark job failed, batch id: " + app.getId());
                        return false;
                    case "success":
                        logger.info("spark job succeed, batch id: " + app.getId());
                        try {
                            lineangeAnalysis(context, app.getAppId());
                        }catch (Exception e){
                            logger.error("failed on lineage analysis", e);
                        }
                        return true;
                    default:
                        logger.error("unknown livy job status -> " + state);
                        return false;
                }
            }

        } catch (Exception e) {
            logger.info("Faild to Execution: ", e);
        }

        return true;
    }

    public void onAbort(OperatorContext context){
        logger.info("Delete spark batch job, id: " + app.getId());
        livyClient.deleteBatch(app.getId());
    }

    public void lineangeAnalysis(OperatorContext context, String applicationId){
        logger.info("Start lineage analysis for batch id: " + app.getId());

        List<DataStore> inlets = new ArrayList<>();
        List<DataStore> outlets = new ArrayList<>();
        String dispatcher = context.getConfig().getString("dispatcher");

        if(dispatcher.equals("hdfs")){
            String hdfsAddr = context.getConfig().getString("hdfsAddr");
            String input = "/tmp/" + applicationId + ".input.txt";
            String output = "/tmp/" + applicationId + ".output.txt";

            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", hdfsAddr);
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
        }else if(dispatcher.equals("s3")){
            try{
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion("ap-northeast-1")
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials("AKIAIL42HPN4LO3XUIHQ", "yFfJ74UD80NWmPuhH2dLKr2JYJU8RU/qj0QVzOE8")
                        ))
                        .build();
                String inputPath = "lineage/" + applicationId + ".input.txt";
                String outputPath = "lineage/" + applicationId + ".output.txt";

                BufferedReader inBufferReader;
                try (S3Object inObject = s3Client.getObject(new GetObjectRequest("com.miotech.data.prd", inputPath))) {
                    inBufferReader = new BufferedReader(new InputStreamReader(inObject.getObjectContent()));
                    inlets.addAll(genDataStore(inBufferReader));
                } catch (IOException e) {
                    logger.error("get s3 inlets file failed", e);
                }
                BufferedReader outBufferReader;
                try (S3Object outObject = s3Client.getObject(new GetObjectRequest("com.miotech.data.prd", outputPath))) {
                    outBufferReader = new BufferedReader(new InputStreamReader(outObject.getObjectContent()));
                    outlets.addAll(genDataStore(outBufferReader));
                } catch (IOException e) {
                    logger.error("get s3 outlets file failed", e);
                }
            }catch (Exception e){
                logger.error("lineage analysis from s3 failed", e);
            }
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
            System.out.println(line);
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
        Integer length = slices.length;
        String url = slices[length - 3];
        String index = slices[length - 2];
        return new ElasticSearchIndexStore(url,index);
    }

    public HiveTableStore getHiveStore(String line){
        String[] slices = line.split("/");
        Integer length = slices.length;
        String url = slices[2];
        String table = slices[length - 1];
        String db = slices[length - 2].split(".")[0];
        return new HiveTableStore(url, db, table);
    }

    public HiveTableStore getPGStore(String line){
        String[] slices = line.split(":");
        String table = slices[slices.length - 1];

        slices = line.split("/");
        Integer length = slices.length;
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
        Integer length = slices.length;
        String table = slices[length - 1];
        String db = slices[length - 2].toLowerCase();
        String url = line;
        return new HiveTableStore(url, db, table);
    }

    @Override
    public ConfigDef config() {
        throw new UnsupportedOperationException("need implementation");
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException("need implementation");
    }
}
