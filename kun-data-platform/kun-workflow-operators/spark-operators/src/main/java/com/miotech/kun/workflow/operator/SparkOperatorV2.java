package com.miotech.kun.workflow.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SparkOperatorV2 extends SparkSubmitBaseOperator {

    @Override
    public List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(SparkOperatorUtils.parseSparkSubmitParmas(sparkSubmitParams));
        cmd.addAll(SparkOperatorUtils.parseSparkConf(sparkConf));
        cmd.add(app);
        cmd.addAll(Arrays.asList(appArgs.trim().split(" +")));
        return cmd;
    }

}
