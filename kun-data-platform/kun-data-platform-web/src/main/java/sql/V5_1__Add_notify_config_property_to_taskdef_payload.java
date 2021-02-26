package sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefNotifyConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class V5_1__Add_notify_config_property_to_taskdef_payload extends BaseJavaMigration {
    private static final Logger logger = LoggerFactory.getLogger(V5_1__Add_notify_config_property_to_taskdef_payload.class);

    private static final TaskDefNotifyConfig NOTIFY_CONFIG_DEFAULT_VALUE = new TaskDefNotifyConfig(
            TaskStatusNotifyTrigger.SYSTEM_DEFAULT,
            Collections.emptyList()
    );

    private static final int CHECKSUM = 1614319554;

    private static final String TABLE_NAME = "kun_dp_task_definition";

    private static final String ID_COL_NAME = "id";

    private static final String PAYLOAD_COL_NAME = "task_payload";

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }

    @Override
    public void migrate(Context context) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String sql = String.format("SELECT %s, %s FROM %s WHERE %s IS NOT NULL", ID_COL_NAME, PAYLOAD_COL_NAME, TABLE_NAME, PAYLOAD_COL_NAME);
        List<Long> ids = new ArrayList<>();
        List<String> payloadJsons = new ArrayList<>();

        try (PreparedStatement stmtSelectExistingPayloadRecords = context.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmtSelectExistingPayloadRecords.executeQuery();
            int count = 0;
            while (rs.next()) {
                Long id = rs.getLong(1);
                String payloadJson = rs.getString(2);
                ids.add(id);
                payloadJsons.add(payloadJson);
                count += 1;
            }

            logger.info("Found {} task definition records with taskdef payload not null.", count);

            int updateCount = 0;
            List<TaskPayload> taskPayloadToUpdate = new ArrayList<>(payloadJsons.size());
            for (String jsonStr : payloadJsons) {
                TaskPayload taskPayload = objectMapper.readValue(jsonStr, TaskPayload.class);
                if (taskPayload.getNotifyConfig() == null) {
                    updateCount += 1;
                    taskPayload = taskPayload.cloneBuilder()
                            .withNotifyConfig(NOTIFY_CONFIG_DEFAULT_VALUE)
                            .build();
                }
                taskPayloadToUpdate.add(taskPayload);
            }
            logger.info("Found {} payload fields with notify config empty. Applying udpate...", updateCount);

            String updateSql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", TABLE_NAME, PAYLOAD_COL_NAME, ID_COL_NAME);
            try (PreparedStatement batchUpdateStatement = context.getConnection().prepareStatement(updateSql)) {
                for (int i = 0; i < count; ++i) {
                    Long id = ids.get(i);
                    TaskPayload updatedPayload = taskPayloadToUpdate.get(i);
                    batchUpdateStatement.setString(1, objectMapper.writeValueAsString(updatedPayload));
                    batchUpdateStatement.setLong(2, id);
                    batchUpdateStatement.addBatch();
                }
                int[] success = batchUpdateStatement.executeBatch();
                logger.info("Update finished. Affected {} rows", countUpdateRows(success));
            } catch (Exception e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        } catch (Exception e) {
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
