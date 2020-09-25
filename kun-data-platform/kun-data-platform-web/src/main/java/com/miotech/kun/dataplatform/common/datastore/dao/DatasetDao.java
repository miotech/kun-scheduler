package com.miotech.kun.dataplatform.common.datastore.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetSearchRequest;
import com.miotech.kun.dataplatform.model.datastore.TaskDataset;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DatasetDao {
    public static final String DATASET_TABLE_NAME = "kun_dp_task_datasets";

    public static final String DATASET_MODEL_NAME = "dataset";

    public static final List<String> datasetCols = ImmutableList.of("id", "dataset_name", "definition_id", "datastore_id");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASET_MODEL_NAME, datasetCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(DATASET_TABLE_NAME, DATASET_MODEL_NAME)
                .autoAliasColumns();
        if (Strings.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<TaskDataset> fetchById(Long id) {
        return jdbcTemplate.query(getSelectSQL(DATASET_MODEL_NAME + ".id = ? "),
                DatasetMapper.INSTANCE, id)
                .stream().findAny();
    }

    public List<TaskDataset> fetchByName(String name) {
        String whereClause = DATASET_MODEL_NAME + ".dataset_name LIKE CONCAT('%', CAST(? AS TEXT), '%')";
        return jdbcTemplate.query(getSelectSQL(whereClause),
                DatasetMapper.INSTANCE, name) ;
    }

    public List<TaskDataset> fetchByDefinitionId(Long definitionId) {
        String whereClause = DATASET_MODEL_NAME + ".definition_id = ?";
        return jdbcTemplate.query(getSelectSQL(whereClause),
                DatasetMapper.INSTANCE, definitionId) ;
    }

    public TaskDataset create(TaskDataset dataset) {
        String createSQL = DefaultSQLBuilder.newBuilder()
                .insert(datasetCols.toArray(new String[0]))
                .into(DATASET_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                createSQL,
                dataset.getId(),
                dataset.getDatasetName(),
                dataset.getDefinitionId(),
                dataset.getDatastoreId()
        );

        return dataset;
    }

    public List<TaskDataset> updateTaskDatasets(Long definitionId, List<TaskDataset> taskDatasets) {
        String deleteSQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(DATASET_TABLE_NAME)
                .where("definition_id = ?")
                .getSQL();

        jdbcTemplate.update(deleteSQL, definitionId);
        String createSQL = DefaultSQLBuilder.newBuilder()
                .insert(datasetCols.toArray(new String[0]))
                .into(DATASET_TABLE_NAME)
                .asPrepared()
                .getSQL();
        List<Object[]> params = taskDatasets.stream()
                .map (x -> new Object[] { x.getId(), x.getDatasetName(), x.getDefinitionId(), x.getDatastoreId() })
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(createSQL, params);
        return taskDatasets;
    }

    public List<TaskDataset> search(DatasetSearchRequest searchRequest) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" 1 = 1");
        List<Object> params = new ArrayList();
        List<Long> datasetIds = searchRequest.getDefinitionIds();
        if (CollectionUtils.isNotEmpty(datasetIds)) {
            whereClause.append(" AND ");
            whereClause.append(String.format(DATASET_MODEL_NAME + ".definition_id in (%s)", StringUtils.repeatJoin("?", ",", datasetIds.size())));
            params.addAll(datasetIds);
        }

        List<Long> datastoreIds = searchRequest.getDatastoreIds();
        if (CollectionUtils.isNotEmpty(datastoreIds)) {
            whereClause.append(" AND ");
            whereClause.append(String.format(DATASET_MODEL_NAME + ".datastore_id in (%s)", StringUtils.repeatJoin("?", ",", datastoreIds.size())));
            params.addAll(datastoreIds);
        }

        if (org.apache.commons.lang3.StringUtils.isNoneBlank(searchRequest.getDatasetName())) {
            whereClause.append(" AND ");
            whereClause.append(DATASET_MODEL_NAME + ".dataset_name LIKE CONCAT('%', CAST(? AS TEXT) , '%')");
            params.add(searchRequest.getDatasetName());
        }
        String sql = DefaultSQLBuilder.newBuilder()
                .select(getSelectSQL(whereClause.toString()))
                .orderBy("id")
                .getSQL();

        // list
        return jdbcTemplate.query(sql, DatasetMapper.INSTANCE, params.toArray());

    }

    public static class DatasetMapper implements RowMapper<TaskDataset> {
        public static final DatasetMapper INSTANCE = new DatasetMapper();

        @Override
        public TaskDataset mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskDataset.newBuilder()
                    .withId(rs.getLong(DATASET_MODEL_NAME + "_id"))
                    .withDatasetName(rs.getString(DATASET_MODEL_NAME + "_dataset_name"))
                    .withDatastoreId(rs.getLong(DATASET_MODEL_NAME + "_datastore_id"))
                    .withDefinitionId(rs.getLong(DATASET_MODEL_NAME + "_definition_id"))
                    .build();
        }
    }
}
