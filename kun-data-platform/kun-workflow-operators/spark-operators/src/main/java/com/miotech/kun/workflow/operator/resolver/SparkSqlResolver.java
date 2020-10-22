package com.miotech.kun.workflow.operator.resolver;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.SparkConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class SparkSqlResolver extends SqlResolver {

    @Override
    protected SqlInfo getSqlInfoFromConfig(Config config) {
        String sql = config.getString(SparkConfiguration.CONF_SPARK_SQL);
        return new SqlInfo(sql, "hive");
    }

    @Override
    protected List<DataStore> toDataStore(Config config, List<String> tables) {
        String dataStoreUrl = config.getString(SparkConfiguration.CONF_SPARK_DATASTORE_URL);
        String dbName = config.getString(SparkConfiguration.CONF_SPARK_DEFAULT_DB);
        List<DataStore> dataStoreList = tables.stream()
                .map(tableName -> toHiveTableStore(dataStoreUrl, dbName, tableName))
                .collect(Collectors.toList());
        return dataStoreList;
    }


    private HiveTableStore toHiveTableStore(String dataStoreUrl, String dbName, String tableName) {
        int index = tableName.indexOf('.');
        if (index > -1 && index < tableName.length() - 1) {
            dbName = tableName.substring(0, index);
            tableName = tableName.substring(index + 1);
        }
        return new HiveTableStore(
                dataStoreUrl,
                dbName,
                tableName
        );
    }
}
