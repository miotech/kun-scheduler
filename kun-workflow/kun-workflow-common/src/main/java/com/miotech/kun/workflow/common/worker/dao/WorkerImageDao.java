package com.miotech.kun.workflow.common.worker.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.workflow.common.worker.filter.WorkerImageFilter;
import com.miotech.kun.workflow.core.model.worker.WorkerImage;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class WorkerImageDao {

    private final DatabaseOperator dbOperator;

    private final String WORKER_IMAGE_TABLE = "kun_wf_worker_image";

    private final List<String> worker_image_columns = ImmutableList.of("id", "image_name", "version", "active", "created_at", "updated_at");

    @Inject
    public WorkerImageDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public WorkerImage saveWorkerImage(WorkerImage workerImage) {
        Preconditions.checkNotNull(workerImage.getId(), "worker image id could not be null");
        Preconditions.checkNotNull(workerImage.getImageName(), "worker image name could not be null");
        Preconditions.checkNotNull(workerImage.getVersion(), "worker image version could not be null");
        Preconditions.checkNotNull(workerImage.getActive(), "worker image active could not be null");

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(worker_image_columns.toArray(new String[0]))
                .into(WORKER_IMAGE_TABLE)
                .asPrepared()
                .getSQL();
        dbOperator.update(sql,
                workerImage.getId(),
                workerImage.getImageName(),
                workerImage.getVersion(),
                workerImage.getActive(),
                DateTimeUtils.now(),
                DateTimeUtils.now());
        return workerImage;
    }

    /**
     * fetch worker image with filter
     * order by id desc
     * @param filter
     * @return
     */
    public List<WorkerImage> fetchWorkerImage(WorkerImageFilter filter) {
        Preconditions.checkNotNull(filter.getPage(), "page could not be null");
        Preconditions.checkNotNull(filter.getPageSize(), "pageSize could not be null");
        SQLBuilder sqlBuilder = DefaultSQLBuilder.newBuilder()
                .select(worker_image_columns.toArray(new String[0]))
                .from(WORKER_IMAGE_TABLE);

        List<String> whereCondition = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (filter.getId() != null) {
            whereCondition.add("id = ?");
            params.add(filter.getId());
        }
        if (filter.getName() != null) {
            whereCondition.add("image_name = ?");
            params.add(filter.getName());
        }
        if (filter.getActive() != null) {
            whereCondition.add("active = ?");
            params.add(filter.getActive());
        }
        if (whereCondition.size() > 0) {
            String whereStr = whereCondition.stream().collect(Collectors.joining(" and "));
            sqlBuilder.where(whereStr);
        }
        Integer pageSize = filter.getPageSize() > 100 ? 100 : filter.getPageSize();
        Integer offset = filter.getPage() * pageSize;
        String sql = sqlBuilder
                .limit(pageSize)
                .offset(offset)
                .orderBy("id desc")
                .getSQL();

        return dbOperator.fetchAll(sql, WorkerImageMapper.WORKER_IMAGE_INSTANCE, params.toArray());
    }

    public Boolean setActiveVersion(Long imageId) {
        String cancelActive = DefaultSQLBuilder.newBuilder()
                .update(WORKER_IMAGE_TABLE)
                .set("active")
                .where("active = ?")
                .asPrepared()
                .getSQL();
        String setActive = DefaultSQLBuilder.newBuilder()
                .update(WORKER_IMAGE_TABLE)
                .set("active")
                .where("id = ?")
                .asPrepared()
                .getSQL();

        return dbOperator.transaction(() -> {
            dbOperator.update(cancelActive, false, true);
            return dbOperator.update(setActive, true, imageId) == 1;
        });
    }

    public static class WorkerImageMapper implements ResultSetMapper<WorkerImage> {

        private static final WorkerImageMapper WORKER_IMAGE_INSTANCE = new WorkerImageMapper();

        @Override
        public WorkerImage map(ResultSet rs) throws SQLException {
            return WorkerImage.newBuilder()
                    .withId(rs.getLong("id"))
                    .withImageName(rs.getString("image_name"))
                    .withVersion(rs.getString("version"))
                    .withActive(rs.getBoolean("active"))
                    .withCreated_at(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                    .withUpdated_at(DateTimeUtils.fromTimestamp(rs.getTimestamp("updated_at")))
                    .build();
        }
    }


}
