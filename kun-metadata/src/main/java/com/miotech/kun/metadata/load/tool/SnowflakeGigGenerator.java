package com.miotech.kun.metadata.load.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.model.entity.DataStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class SnowflakeGigGenerator extends DatasetGidGenerator {
    private static Logger logger = LoggerFactory.getLogger(SnowflakeGigGenerator.class);

    private final DatabaseOperator dbOperator;

    @Inject
    public SnowflakeGigGenerator(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    @Override
    public long generate(DataStore dataStore) {
        // Convert dataStore to JSON
        String dataStoreJson;
        try {
            dataStoreJson = DataStoreJsonUtil.toJson(dataStore);
        } catch (JsonProcessingException e) {
            logger.error("serialize fail, dataStoreType: " + dataStore.getType());
            throw new RuntimeException("serialize fail, ", e);
        }

        Long gid = dbOperator.fetchOne("SELECT dataset_gid FROM kun_mt_dataset_gid WHERE data_store = ?", rs -> rs.getLong(1), dataStoreJson);
        if (gid != null && gid > 0) {
            return gid;
        } else {
            gid = IdGenerator.getInstance().nextId();
            dbOperator.update("INSERT INTO kun_mt_dataset_gid(data_store, dataset_gid) VALUES (?, ?)", dataStoreJson, gid);
        }

        return gid;
    }

}
