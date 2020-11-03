package sql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;

public class V6__Flatten_config_JSON extends BaseJavaMigration {
    private static final Logger logger = LoggerFactory.getLogger(V6__Flatten_config_JSON.class);

    private static final TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};

    private static final int CHECKSUM = 87370752;

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }

    @Override
    public void migrate(Context context) throws Exception {
        migrateTable(context, "kun_wf_task");
        migrateTable(context, "kun_wf_task_run");
    }

    private void migrateTable(Context context, String tableName) throws SQLException {
        String sqlCount = String.format("SELECT count(*) FROM %s", tableName);
        Integer count;
        try (PreparedStatement statement = context.getConnection().prepareStatement(sqlCount)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = 0;
            }
        }
        logger.info("Detected {} rows of config JSON to update.", count);
        int batchSize = 1000;
        for (int i = 0; i * batchSize < count; ++i) {
            List<Pair<Long, String>> updatePairs = new LinkedList<>();
            String sqlSelect = String.format("SELECT id, config FROM %s ORDER BY id ASC LIMIT %s OFFSET %s", tableName, batchSize, i * batchSize);
            try (PreparedStatement statement = context.getConnection().prepareStatement(sqlSelect)) {
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    Long id = rs.getLong(1);
                    String configJson = rs.getString(2);
                    if (isValidConfigJson(configJson)) {
                        Map<String, Object> parsedObject = JSONUtils.jsonStringToMap(configJson);
                        String updatedJson = JSONUtils.toJsonString((Map<String, Object>) parsedObject.get("values"), typeRef);
                        updatePairs.add(Pair.of(id, updatedJson));
                    }
                }
            }
            String sqlUpdate = String.format("UPDATE %s SET config = ? WHERE id = ?", tableName);
            try (PreparedStatement statement = context.getConnection().prepareStatement(sqlUpdate)) {
                for (Pair<Long, String> updatePair : updatePairs) {
                    statement.setString(1, updatePair.getRight());
                    statement.setLong(2, updatePair.getLeft());
                    statement.addBatch();
                }
                int[] affectedRows = statement.executeBatch();
                logger.info("Updating config JSON schema. Affected {} rows.", countUpdateRows(affectedRows));
            }
        }
    }

    private boolean isValidConfigJson(String configJson) {
        if (StringUtils.isBlank(configJson)) {
            return false;
        }
        try {
            Map<String, Object> parsedObject = JSONUtils.jsonStringToMap(configJson);
            return parsedObject.entrySet().size() == 1 && parsedObject.containsKey("values");
        } catch (Exception e) {
            return false;
        }
    }

    private int countUpdateRows(int[] affectedRows) {
        int sum = 0;
        for (int rows : affectedRows) {
            sum += rows;
        }
        return sum;
    }
}
