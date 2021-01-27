package com.miotech.kun.metadata.databuilder.service.gid;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DSI;
import com.miotech.kun.metadata.core.model.DataStore;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GidService {
    private final DatabaseOperator dbOperator;

    @Inject
    public GidService(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public long generate(DataStore dataStore) {
        // Convert dataStore to JSON
        Preconditions.checkNotNull(dataStore, "dataStore can't be null");

        String dataStoreJson = DataStoreJsonUtil.toJson(dataStore);
        Long gid = getByDSI(dataStore.getDSI());

        if (gid != null && gid > 0) {
            return gid;
        } else {
            gid = IdGenerator.getInstance().nextId();
            dbOperator.update("INSERT INTO kun_mt_dataset_gid(data_store, dataset_gid, dsi) VALUES (CAST(? AS JSONB), ?, ?)",
                    dataStoreJson,
                    gid,
                    dataStore.getDSI().toFullString()
            );
        }

        return gid;
    }

    public Long getByDSI(DSI dsi) {
        // Convert dataStore to JSON
        Preconditions.checkNotNull(dsi, "DSI cannot be null");

        return dbOperator.fetchOne(
                "SELECT dataset_gid FROM kun_mt_dataset_gid WHERE dsi LIKE CONCAT(CAST(? AS TEXT), '%')",
                rs -> rs.getLong(1),
                dsi.toEssentialString()
        );
    }
}
