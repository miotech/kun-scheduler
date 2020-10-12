package com.miotech.kun.workflow.client.operator;


import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.*;

@Singleton
public class OperatorUpload {

    private WorkflowClient clientUtil;

    private static Logger logger = LoggerFactory.getLogger(OperatorUpload.class);

    private static final int BUFFER_SIZE = 8192;


    public OperatorUpload(String workflowUrl) {
        this.clientUtil = new DefaultWorkflowClient(workflowUrl);
    }

    public OperatorUpload(WorkflowClient clientUtil) {
        this.clientUtil = clientUtil;
    }

    public List<Operator> autoUpload() {
        return scanOperatorJar();
    }

    public List<OperatorDef> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = PropsUtils.class
                .getClassLoader()
                .getResourceAsStream("operator.yaml");
        Map<String, Object> yamlProps = yaml.load(inputStream);
        List<LinkedHashMap<String, String>> mapList = (ArrayList) yamlProps.get("operators");
        List<OperatorDef> operatorDefList = new ArrayList<>();
        for (Map<String, String> map : mapList) {
            OperatorDef operatorDef = new OperatorDef();
            operatorDef.setName(map.get("name"));
            operatorDef.setClassName(map.get("className"));
            operatorDef.setDescription(map.get("description"));
            operatorDefList.add(operatorDef);
        }
        return operatorDefList;
    }


    public List<Operator> scanOperatorJar() {
        List<OperatorDef> operatorDefList = loadConfig();
        logger.info("load operator from operator.yaml, operatorDefList:{}", operatorDefList);
        Map<String, Operator> exitingOperators = getExistOperator();
        List<Operator> uploadOperator = new ArrayList<>();
        for (OperatorDef operatorDef : operatorDefList) {
            String operatorName = operatorDef.getName();
            if (exitingOperators.containsKey(operatorName)) {
                Operator existOperator = exitingOperators.get(operatorName);
                operatorDef.setId(existOperator.getId());
            }
            URL url = this.getClass().getClassLoader().getResource(operatorDef.getName() + ".jar");
            logger.info("scan operator jar from URL:{}",url);
            if (url != null) {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(operatorDef.getName() + ".jar");
                File file = new File("/tmp/" + operatorDef.getName() + ".jar");
                try {
                    streamToFile(inputStream, file);
                } catch (IOException e) {
                    logger.error("load file : {} from inputStream failed", file.getName(), e);
                }
                logger.info("start to upload operator : {},file:{}", operatorDef.toString(), file.getName());
                Operator operator = createOperator(operatorDef.toOperator(), file);
                uploadOperator.add(operator);
            } else {
                logger.info("can't find operator jar, operator name :{} ", operatorDef.getName());
            }
        }
        return uploadOperator;

    }

    private void streamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();


    }

    private void uploadJar(Long operatorId, File file) {
        try {
            clientUtil.uploadOperatorJar(operatorId, file);
            logger.info("upload operator jar success, operatorId = {}",operatorId);
        } catch (Exception e) {
            logger.error("upload operator jar failed, operatorId = {}", operatorId, e);
        }

    }

    public Map<String, Operator> getExistOperator() {
        Map<String, Operator> exitingOperators = new HashMap<>();
        try {
            List<Operator> existOperatorList = clientUtil.getExistOperators();
            for (Operator operator : existOperatorList) {
                exitingOperators.put(operator.getName(), operator);
            }
        } catch (Exception e) {
            logger.error("get exist operator filed", e);
        }
        return exitingOperators;
    }

    private Operator createOperator(Operator operatorDef, File file) {
        Operator operator;
        if (operatorDef.getId() == null) {
            operator = clientUtil.saveOperator(operatorDef.getName(), operatorDef);
            logger.info("create operator,operatorName : {}",operator.getName());
            uploadJar(operator.getId(), file);
        } else {
            uploadJar(operatorDef.getId(), file);
            operator = clientUtil.updateOperator(operatorDef.getId(), operatorDef);
            logger.info("update operator,operatorId : {}",operator.getId());
        }
        return operator;
    }


}
