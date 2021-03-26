package com.miotech.kun.workflow.common.workerInstance;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceEnv;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Singleton
public class WorkerInstanceDao {

    protected static final String WORKER_INSTANCE_TABLE = "kun_wf_worker_instance";

    private static final List<String> workerInstanceCols = ImmutableList.of("task_attempt_id", "worker_id", "env", "created_at", "updated_at");

    private final DatabaseOperator dbOperator;
    private final WorkerInstanceMapper workerInstanceMapper = new WorkerInstanceMapper();

    @Inject
    public WorkerInstanceDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public WorkerInstance createWorkerInstance(WorkerInstance workerInstance) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(workerInstanceCols.toArray(new String[0]))
                .into(WORKER_INSTANCE_TABLE)
                .asPrepared()
                .getSQL();
        OffsetDateTime now = DateTimeUtils.now();
        dbOperator.update(sql,
                workerInstance.getTaskAttemptId(),
                workerInstance.getWorkerId(),
                workerInstance.getEnv().name(),
                now,
                now);
        return workerInstance;
    }

    public WorkerInstance updateWorkerInstance(WorkerInstance workerInstance) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(WORKER_INSTANCE_TABLE)
                .set("worker_id", "updated_at")
                .where("task_attempt_id = ? and env = ?")
                .asPrepared()
                .getSQL();
        OffsetDateTime now = DateTimeUtils.now();
        dbOperator.update(sql,
                workerInstance.getWorkerId(),
                now,
                workerInstance.getTaskAttemptId(),
                workerInstance.getEnv().name());
        return workerInstance;

    }

    public Boolean deleteWorkerInstance(long taskAttemptId, WorkerInstanceEnv env) {
        String sql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(WORKER_INSTANCE_TABLE)
                .where("task_attempt_id = ? and env = ?")
                .asPrepared()
                .getSQL();
        return dbOperator.update(sql, taskAttemptId, env.name()) > 0;
    }

    public WorkerInstance getWorkerInstanceByAttempt(Long taskAttemptId) {
        String sql = new DefaultSQLBuilder()
                .select(workerInstanceCols.toArray(new String[0]))
                .from(WORKER_INSTANCE_TABLE)
                .where("task_attempt_id=?")
                .orderBy("updated_at desc")
                .limit()
                .autoAliasColumns()
                .asPrepared()
                .getSQL();
        List<WorkerInstance> workerInstances = dbOperator.fetchAll(sql, workerInstanceMapper, taskAttemptId, 1);
        if (workerInstances.size() == 0) {
            return null;
        } else {
            return workerInstances.get(0);
        }
    }

    public List<WorkerInstance> getActiveWorkerInstance(WorkerInstanceEnv env) {
        String sql = new DefaultSQLBuilder()
                .select(workerInstanceCols.toArray(new String[0]))
                .from(WORKER_INSTANCE_TABLE)
                .where("env = ?")
                .getSQL();
        return dbOperator.fetchAll(sql, workerInstanceMapper, env.name());
    }

    private static class WorkerInstanceMapper implements ResultSetMapper<WorkerInstance> {

        @Override
        public WorkerInstance map(ResultSet rs) throws SQLException {
            return WorkerInstance.newBuilder()
                    .withTaskAttemptId(rs.getLong("task_attempt_id"))
                    .withEnv(WorkerInstanceEnv.valueOf(rs.getString("env")))
                    .withWorkerId(rs.getString("worker_id"))
                    .build();
        }
    }
}
