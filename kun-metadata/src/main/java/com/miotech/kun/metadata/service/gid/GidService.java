package com.miotech.kun.metadata.service.gid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GidService {
    private static Logger logger = LoggerFactory.getLogger(GidService.class);

    private final DatabaseOperator dbOperator;

    @Inject
    public GidService(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public long generate(DataStore dataStore) {
        // Convert dataStore to JSON
        if (dataStore == null) {
            throw new RuntimeException("dataStore can't be null");
        }

        String dataStoreJson;
        try {
            dataStoreJson = DataStoreJsonUtil.toJson(dataStore);
        } catch (JsonProcessingException e) {
            logger.error("serialize fail, dataStoreType: " + dataStore.getType());
            throw new RuntimeException("serialize fail, ", e);
        }

        Long gid = dbOperator.fetchOne("SELECT dataset_gid FROM kun_mt_dataset_gid WHERE data_store = ?::jsonb", rs -> rs.getLong(1), dataStoreJson);
        if (gid != null && gid > 0) {
            return gid;
        } else {
            gid = IdGenerator.getInstance().nextId();
            dbOperator.update("INSERT INTO kun_mt_dataset_gid(data_store, dataset_gid) VALUES (?::jsonb, ?)", dataStoreJson, gid);
        }

        return gid;
    }

}
