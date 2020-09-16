package com.miotech.kun.workflow.operator.resolver;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.SparkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SparkSqlResolver extends SqlResolver implements Resolver {

    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        return getDataStoreList(config,LineageDirection.UP_STREAM);
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        return getDataStoreList(config,LineageDirection.DOWN_STREAM);
    }

    private List<DataStore> getDataStoreList(Config config,LineageDirection direction){
        String sql = config.getString(SparkConfiguration.CONF_SPARK_SQL);
        List<String> tableNameList = new ArrayList<>();
        if(direction.equals(LineageDirection.UP_STREAM)){
            tableNameList = getUpStream(sql,"hive");
        }else if(direction.equals(LineageDirection.DOWN_STREAM)){
            tableNameList = getDownStream(sql,"hive");
        }
        String dataStoreUrl = config.getString(SparkConfiguration.CONF_SPARK_DATASTORE_URL);
        String dbName = config.getString(SparkConfiguration.CONF_SPARK_DEFAULT_DB);
        List<DataStore> dataStoreList = tableNameList.stream()
                .map(tableName->toHiveTableStore(dataStoreUrl,dbName,tableName))
                .collect(Collectors.toList());
        return dataStoreList;
    }

    private HiveTableStore toHiveTableStore(String dataStoreUrl,String dbName,String tableName){
        int index = tableName.indexOf('.');
        if(index > -1 && index < tableName.length() - 1){
            dbName = tableName.substring(0,index);
            tableName = tableName.substring(index + 1);
        }
        return new HiveTableStore(
                dataStoreUrl,
                dbName,
                tableName
        );
    }
}
