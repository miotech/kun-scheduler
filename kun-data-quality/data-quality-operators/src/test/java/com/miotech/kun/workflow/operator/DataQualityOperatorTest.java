package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Ignore
public class DataQualityOperatorTest {

    private OperatorRunner operatorRunner;;

    @Before
    public void setUp(){
        KunOperator operator = new DataQualityOperator();
        operatorRunner = new OperatorRunner(operator);

        Map<String ,String> params = new HashMap<>();
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_URL, "jdbc:postgresql://localhost:5432/kun");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME, "postgres");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD, "password");
        params.put(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS, "org.postgresql.Driver");
        params.put("caseId", "74193442078457856");
        operatorRunner.setConfig(params);
    }

    @Test
    public void run() {
        operatorRunner.run();
        for (String s : operatorRunner.getLog()) {
            System.out.println(s);
        }
    }
}
