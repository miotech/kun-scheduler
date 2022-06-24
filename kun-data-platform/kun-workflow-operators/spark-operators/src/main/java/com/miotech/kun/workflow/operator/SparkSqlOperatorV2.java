package com.miotech.kun.workflow.operator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.miotech.kun.workflow.operator.utils.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkSqlOperatorV2 extends SparkSubmitBaseOperator {
    private static final Logger logger = LoggerFactory.getLogger(SparkSqlOperatorV2.class);


    @Override
    public List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(SparkOperatorUtils.parseSparkSubmitParmas(sparkSubmitParams));

        Map<String, Object> exprArgs = new HashMap<>();
        exprArgs.put("execute_time", getContext().getScheduleTime());
        exprArgs.put("target", getContext().getExecuteTarget());
        String sql = ExprUtils.evalExpr(appArgs, exprArgs);

        File sqlFile = storeSqlToFile(sql);
        addSqlFile(sparkConf, sqlFile.getPath());
        cmd.addAll(SparkOperatorUtils.parseSparkConf(sparkConf));
        cmd.add(app);
        cmd.add("-f");
        cmd.add(sqlFile.getName());
        return cmd;
    }

    private void addSqlFile(Map<String, String> sparkConf, String sqlFile) {
        String files = sparkConf.getOrDefault("spark.files", "");
        if (files.equals("")) {
            sparkConf.put("spark.files", sqlFile);
        } else {
            sparkConf.put("spark.files", files + "," + sqlFile);
        }
    }

    private File storeSqlToFile(String sql) {
        File sqlFile = null;
        try {
            sqlFile = File.createTempFile("spark-sql-", ".sql");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile))) {
                writer.write(sql);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sqlFile;
    }
}
