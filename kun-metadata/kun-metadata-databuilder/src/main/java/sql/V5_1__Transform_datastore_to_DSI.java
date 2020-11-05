package sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * A migration that converts `datastore` column to `dsi` column in following tables:
 * kun_mt_dataset
 * kun_mt_dataset_gid
 * This migration will fill up ALL the empty
 */
@SuppressWarnings("ALL")
public class V5_1__Transform_datastore_to_DSI extends BaseJavaMigration {
    private static final Logger logger = LoggerFactory.getLogger(V5_1__Transform_datastore_to_DSI.class);

    /**
     * Change this checksum if you want to trigger flyway's change detection,
     * since flyway cannot compute the checksum of a java class migration automatically.
     * DO NOT CHANGE THIS VALUE if you don't understand the mechanism of flyway's validation.
     * See {@link https://flywaydb.org/documentation/concepts/migrations#checksums-and-validation}
     **/
    private static final int CHECKSUM = 140426559;

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }

    @Override
    public void migrate(Context context) throws Exception {
        fillDSIFieldForDatasetTable(context, "kun_mt_dataset", "gid");
        fillDSIFieldForDatasetTable(context, "kun_mt_dataset_gid", "dataset_gid");
    }

    private void fillDSIFieldForDatasetTable(Context context, String tableName, String primaryKeyColumn) throws SQLException {
        logger.info("Migrating `datastore` column to `dsi` column for table `{}`...", tableName);
        Integer updateCount;

        try (PreparedStatement stmtCountKunMTDataset = context.getConnection().prepareStatement(String.format("SELECT count(*) FROM %s WHERE dsi IS NULL", tableName))) {
            ResultSet rs = stmtCountKunMTDataset.executeQuery();
            if (rs.next()) {
                updateCount = rs.getInt(1);
            } else {
                updateCount = 0;
            }
            logger.info("Table `{}`: Found {} results where `dsi` column is empty...", tableName, updateCount);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        int batchSize = 1000;

        for (int i = 0; i * batchSize < updateCount; ++i) {
            String sql = String.format("SELECT %s, data_store FROM %s WHERE dsi IS NULL ORDER BY %s ASC LIMIT %s", primaryKeyColumn, tableName, primaryKeyColumn, batchSize);
            List<Pair<Long, DataStore>> gidAndDatastores = new LinkedList<>();

            try (PreparedStatement stmt = context.getConnection().prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Long pk = rs.getLong(1);
                    DataStore dataStore = parseDataStoreJSON(rs.getString(2));
                    gidAndDatastores.add(Pair.of(pk, dataStore));
                }
            } catch (Exception e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }

            String updateSql = String.format("UPDATE %s SET dsi = ? WHERE %s = ?", tableName, primaryKeyColumn);
            try (PreparedStatement batchUpdateStatement = context.getConnection().prepareStatement(updateSql)) {
                for (Pair<Long, DataStore> pair : gidAndDatastores) {
                    batchUpdateStatement.setString(1, pair.getRight().getDSI().toFullString());
                    batchUpdateStatement.setLong(2, pair.getLeft());
                    batchUpdateStatement.addBatch();
                }
                int[] success = batchUpdateStatement.executeBatch();
                logger.info("Update finished. Affected {} rows", countUpdateRows(success));
            } catch (Exception e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }

    private DataStore parseDataStoreJSON(String datastoreJson) {
        try {
            return DataStoreJsonUtil.toDataStore(datastoreJson);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private int countUpdateRows(int[] successRows) {
        int sum = 0;
        for (int success : successRows) {
            sum += success;
        }
        return sum;
    }
}
