package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
public class DataQualityOperatorTest {

    private OperatorRunner operatorRunner;;

    @Before
    public void setUp(){
        Operator operator = new DataQualityOperator();
        operatorRunner = new OperatorRunner(operator);

        Map<String ,String> params = new HashMap<>();
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_URL, "jdbc:postgresql://localhost:5432/kun");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME, "postgres");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD, "password");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS, "org.postgresql.Driver");
        params.put("caseId", "74193442078457856");
        operatorRunner.setParams(params);
    }

    @Test
    public void run() {
        operatorRunner.run();
        for (String s : operatorRunner.getLog()) {
            System.out.println(s);
        }
    }
}
