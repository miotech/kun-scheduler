package com.miotech.kun.workflow.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkSubmitOperator extends SparkSubmitBaseOperator {

    @Override
    public String buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(parseSparkSubmitParmas(sparkSubmitParams));
        cmd.addAll(parseSparkConf(sparkConf));
        cmd.add(surroundWithQuotes(app));
        cmd.add(surroundWithQuotes(appArgs));
        return String.join(" ", cmd);
    }

}
