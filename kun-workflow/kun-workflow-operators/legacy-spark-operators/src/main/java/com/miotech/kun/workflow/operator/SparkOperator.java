package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.model.entity.Entity;
import com.miotech.kun.workflow.operator.model.clients.SparkClient;
import com.miotech.kun.workflow.operator.model.models.SparkApp;
import com.miotech.kun.workflow.operator.model.models.SparkJob;
import com.miotech.kun.workflow.operator.model.clients.LivyClient;
import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.utils.StringUtils;

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

public class SparkOperator implements Operator {

    SparkJob job = new SparkJob();
    Logger logger;
    SparkApp app;
    LivyClient livyClient;
    SparkClient sparkClient;

    public void init(OperatorContext context){
        logger = context.getLogger();

        String jars = context.getParameter("jars");
        String files = context.getParameter("files");
        String application = context.getParameter("application");
        String args = context.getParameter("args");

        String sessionName = context.getParameter("name");
        if(!StringUtils.isNullOrEmpty(sessionName)){
            job.setName(sessionName);
        }
        if (!StringUtils.isNullOrEmpty(jars)) {
            job.setJars(Arrays.asList(jars.split(",")));
        }
        List<String> jobFiles = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(files)) {
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
        if (!StringUtils.isNullOrEmpty(application)) {
            job.setClassName(application);
        }
        List<String> jobArgs = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(args)) {
            List<String> trimArgs = Arrays.stream(args.split("\\s+"))
                    .filter(x -> !x.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            jobArgs = Arrays.stream(trimArgs.toArray(new String[0]))
                    .map(x -> x.startsWith("$") ? context.getVariable(x.substring(1)) : x)
                    .collect(Collectors.toList());
        }

        if (!jobArgs.isEmpty()) {
            job.setArgs(jobArgs);
        }

        String livy_host = context.getParameter("LIVY_HOST");
        livyClient = new LivyClient(livy_host);
        String spark_host = context.getParameter("SPARK_MASTER");
        String spark_port = context.getParameter("SPARK_PORT");
        sparkClient = new SparkClient(spark_host, Integer.parseInt(spark_port));
    }

    public boolean run(OperatorContext context){

        try {
            app = livyClient.runSparkJob(job);
            int jobId = app.getId();
            logger.info("Execute spark application using livy : batch id {}", app.getId());
            logger.info("Execution job : {}", JSONUtils.toJsonString(job));
            while(true) {
                String state = app.getState();
                if(state == null)
                    // job might be deleted
                    return false;
                switch (state) {
                    case "not_started":
                    case "starting":
                    case "busy":
                    case "idle":
                        System.out.println("running");
                        app = livyClient.getSparkJob(jobId);
                        Thread.sleep(10000);
                        break;
                    case "shutting_down":
                    case "killed":
                    case "dead":
                    case "error":
                        System.out.println("fail");
                        return false;
                    case "success":
                        System.out.println("success");
                        return true;
                }
            }

        } catch (Exception e) {
            logger.info("Faild to Execution: ", e);
        }

        return true;
    }

    public void onAbort(OperatorContext context){
        livyClient.deleteBatch(app.getId());
    }

    public void lineangeAnalysis(OperatorContext context, String applicationId){
        List<Entity> inlets = new ArrayList<>();
        List<Entity> outlets = new ArrayList<>();
        String input = "/tmp" + applicationId + ".input.txt";
        String output = "/tmp/" + applicationId + ".output.txt";

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:8020");

        try {
            FileSystem fs = FileSystem.get(conf);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(new Path(input))));
            String line;
            line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine();
            }
        }catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }

//        context.report(inlets, outlets);
    }

}
