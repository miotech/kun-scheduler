package com.miotech.kun.workflow.client.operator;

import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.mock.MockKunWebServerTestBase;
import com.miotech.kun.workflow.client.model.Operator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class OperatorUploadTest extends MockKunWebServerTestBase {


    private WorkflowClient clientUtil;

    private OperatorUpload operatorUpload;

    @Before
    public void init(){
        clientUtil = new DefaultWorkflowClient(getBaseUrl());
        operatorUpload = new OperatorUpload(clientUtil);
    }

    @Test
    public void loadConfigTest(){
        List<OperatorDef> operatorDefList = operatorUpload.loadConfig();
        Operator operatorDef = Operator.newBuilder()
                .withName("test-operator")
                .withClassName("com.miotech.kun.workflow.operator.BashOperator")
                .withDescription("new operator")
                .build();
        assertThat(operatorDefList,containsInAnyOrder(OperatorDef.fromOperator(operatorDef)));
    }

    @Test
    public void testUploadOperator(){
        Operator newOperator = Operator.newBuilder()
                .withName("test-operator")
                .withClassName("com.miotech.kun.workflow.operator.BashOperator")
                .withDescription("new operator")
                .build();
        Operator existOperator = prepareOperator("test-operator");
        List<Operator> uploadOperators = operatorUpload.scanOperatorJar();
        assertThat(uploadOperators.get(0).getDescription(),is("new operator"));
    }

    private Operator prepareOperator(String name){
        Operator operator = Operator.newBuilder()
                .withName(name)
                .withClassName("com.miotech.kun.workflow.operator.BashOperator")
                .withDescription("exist operator")
                .build();
        Operator newOperator =  clientUtil.saveOperator(operator.getName(),operator);
        return newOperator;
    }

    private void cleanOperator(Long id){
        clientUtil.deleteOperator(id);
    }

}
