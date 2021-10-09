package com.miotech.kun.workflow.operator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkSqlOperatorV2 extends SparkSubmitBaseOperator {

    @Override
    public List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(SparkOperatorUtils.parseSparkSubmitParmas(sparkSubmitParams));

        File sqlFile = storeSqlToFile(appArgs);
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