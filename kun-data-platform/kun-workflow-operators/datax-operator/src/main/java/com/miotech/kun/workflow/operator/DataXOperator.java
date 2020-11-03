package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static com.miotech.kun.workflow.operator.constant.OperatorConfigKey.*;

public class DataXOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(DataXOperator.class);
    private OperatorContext operatorContext;

    @Override
    public void init() {
        operatorContext = getContext();
    }

    @Override
    public boolean run() {
        // Generate jobJsonFile
        File jobJsonFile;
        try {
            jobJsonFile = File.createTempFile("datax-", "-job");
            FileUtils.writeStringToFile(jobJsonFile, operatorContext.getConfig().getString(JOB_JSON), Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Write JobJsonStr to File Error:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        logger.info("Write JobJsonStr to File: {} success", jobJsonFile.getPath());

        // Create a Process to execute the Task
        int exitValue = transfer(jobJsonFile, operatorContext.getConfig().getString(SOURCE_TABLE));
        logger.info("DataX process exitValue: {}", exitValue);

        // Delete temp file
        try {
            Files.delete(jobJsonFile.toPath());
        } catch (IOException e) {
            logger.warn("Delete temp file: {} fail", jobJsonFile.getPath());
        }

        return exitValue == 0;
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigDef config() {
        ConfigDef dataXConfigDef = new ConfigDef();
        dataXConfigDef.define(JOB_JSON, ConfigDef.Type.STRING, true, "Json file needed for DataX operation", JOB_JSON);
        dataXConfigDef.define(SOURCE_TABLE, ConfigDef.Type.STRING, true, "Source Table", SOURCE_TABLE);
        dataXConfigDef.define(SOURCE_TABLE_GID, ConfigDef.Type.LONG, true, "The gid of Source Table", SOURCE_TABLE_GID);
        dataXConfigDef.define(TARGET_TABLE, ConfigDef.Type.STRING, true, "Target Table", TARGET_TABLE);
        dataXConfigDef.define(TARGET_TABLE_GID, ConfigDef.Type.LONG, true, "The gid of Target Table", TARGET_TABLE_GID);
        return dataXConfigDef;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    private int transfer(File jobJsonFile, String sourceTable) {
        try {
//            String dataXPath = this.getClass().getClassLoader().getResource("datax/bin/datax.py").getPath();
            String dataXPath = "/server/lib/datax/bin/datax.py";
            String command = String.format("python %s %s", dataXPath, jobJsonFile.getPath());
            logger.info("Command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            recordProcessLog(process, sourceTable);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            logger.error("TransferJob exec error:", e);
            return 1;
        }
    }

    private void recordProcessLog(Process exec, String table) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logger.info("Table: {}, Msg: {}", table, line);
            }
        } catch (IOException e) {
            logger.warn("Process InputStream has closed...");
        }
    }

}
