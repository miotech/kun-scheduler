package com.miotech.kun.infra.service;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

@Singleton
public class OperatorUploadService {

    @Inject
    private WorkflowServiceFacade workflowServiceFacade;

    private static Logger logger = LoggerFactory.getLogger(OperatorUploadService.class);

    private static final int BUFFER_SIZE = 8192;


    
    public List<Operator> autoUpload() {
        return scanOperatorJar();
    }

    public List<Operator> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = PropsUtils.class
                .getClassLoader()
                .getResourceAsStream("operator.yaml");
        Map<String, Object> yamlProps = yaml.load(inputStream);
        List<LinkedHashMap<String, String>> mapList = (ArrayList) yamlProps.get("operators");
        List<Operator> operatorList = new ArrayList<>();
        for (Map<String, String> map : mapList) {
            Operator operator = Operator.newBuilder()
                    .withName(map.get("name"))
                    .withClassName(map.get("className"))
                    .withDescription(map.get("description"))
                    .build();
            operatorList.add(operator);
        }
        return operatorList;
    }


    public List<Operator> scanOperatorJar() {
        List<Operator> operatorList = loadConfig();
        logger.info("load operator from operator.yaml, operatorList:{}", operatorList);
        Map<String, Operator> exitingOperators = getExistOperator();
        List<Operator> uploadOperator = new ArrayList<>();
        for (Operator operator : operatorList) {
            String operatorName = operator.getName();
            if (exitingOperators.containsKey(operatorName)) {
                Operator existOperator = exitingOperators.get(operatorName);
                operator = operator.cloneBuilder().withId(existOperator.getId()).build();
            }
            URL url = this.getClass().getClassLoader().getResource(operator.getName() + ".jar");
            logger.info("scan operator jar from URL:{}",url);
            if (url != null) {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(operator.getName() + ".jar");
                File file = new File("/tmp/" + operator.getName() + ".jar");
                streamToFile(inputStream, file);
                logger.info("start to upload operator : {},file:{}", operator.toString(), file.getName());
                Operator newOperator = createOperator(operator, file);
                uploadOperator.add(newOperator);
            } else {
                logger.info("can't find operator jar, operator name :{} ", operator.getName());
            }
        }
        return uploadOperator;

    }

    private void streamToFile(InputStream ins, File file){
        try (OutputStream os = new FileOutputStream(file)){
            int bytesRead = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }catch (Exception e){
            logger.error("load file : {} from inputStream failed", file.getName(), e);
        }


    }

    private void uploadJar(Long operatorId, File file) {
        try {
            workflowServiceFacade.uploadOperatorJar(operatorId, file);
            logger.info("upload operator jar success, operatorId = {}",operatorId);
        } catch (Exception e) {
            logger.error("upload operator jar failed, operatorId = {}", operatorId, e);
        }

    }

    public Map<String, Operator> getExistOperator() {
        Map<String, Operator> exitingOperators = new HashMap<>();
        try {
            List<Operator> existOperatorList = workflowServiceFacade.getOperators();
            for (Operator operator : existOperatorList) {
                exitingOperators.put(operator.getName(), operator);
            }
        } catch (Exception e) {
            logger.error("get exist operator filed", e);
        }
        return exitingOperators;
    }

    private Operator createOperator(Operator operator, File file) {
        if (operator.getId() == null) {
            operator = workflowServiceFacade.saveOperator(operator.getName(), operator);
            logger.info("create operator,operatorName : {}",operator.getName());
            uploadJar(operator.getId(), file);
        } else {
            uploadJar(operator.getId(), file);
            operator = workflowServiceFacade.updateOperator(operator.getId(), operator);
            logger.info("update operator,operatorId : {}",operator.getId());
        }
        return operator;
    }


}

