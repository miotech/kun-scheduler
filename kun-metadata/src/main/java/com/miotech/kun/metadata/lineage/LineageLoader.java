package com.miotech.kun.metadata.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.event.LineageEvent;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class LineageLoader {
    private static Logger logger = LoggerFactory.getLogger(LineageLoader.class);

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    public LineageLoader(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public void saveToDB(LineageEvent lineageEvent){
        List<Long> inletDataSetIds = new ArrayList<>();
        List<Long> outletDataSetIds = new ArrayList<>();
        long taskId = lineageEvent.getTaskId();
        for(DataStore store : lineageEvent.getInlets()){
            long id = findDataSetId(store);
            if (id > 0) inletDataSetIds.add(id);
        }
        for(DataStore store : lineageEvent.getOutlets()){
            long id = findDataSetId(store);
            if (id > 0) outletDataSetIds.add(id);
        }
        System.out.println("save to db, taskId: " + taskId);

        Object[][] params = new Object[inletDataSetIds.size()][];
        for(int i = 0; i < inletDataSetIds.size(); i++){
            List<Object> param = new ArrayList<>();
            long inlet = inletDataSetIds.get(i);
            for(long outlet : outletDataSetIds){
                param.add(inlet);
                param.add(outlet);
                param.add(taskId);
            }
            params[i] = param.toArray();
        }
        dbOperator.batch("INSERT INTO kun_mt_dataset_relations(upstream_dataset_gid, downstream_dataset_gid, task_id) VALUES (?, ?, ?)",
                params);

    }

    private long findDataSetId(DataStore store){
        String dataStoreJson = null;
        try {
            dataStoreJson = DataStoreJsonUtil.toJson(store);
        } catch (JsonProcessingException e) {
            logger.error("unknown data store error", e);
            return 0;
        }
        Long gid = dbOperator.fetchOne("SELECT gid FROM kun_mt_dataset WHERE data_store = ?::jsonb", rs -> rs.getLong(1), dataStoreJson);
        if (gid != null && gid > 0) {
            return gid;
        }
        return 0;
    }
}
