package com.miotech.kun.workflow.operator.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
@Singleton
public class DataQualityClient {

    private static final String EXPECTATION_TABLE_NAME = "kun_dq_expectation";
    private static final String EXPECTATION_MODEL_NAME = "kde";
    private static final String EXPECTATION_TEMPLATE_TABLE_NAME = "kun_dq_expectation_template";
    private static final String EXPECTATION_TEMPLATE_MODEL_NAME = "kdet";
    private static final List<String> EXPECTATION_COLUMNS = ImmutableList.of("id", "name", "types", "description", "granularity",
            "template_name", "payload", "trigger", "dataset_gid", "task_id", "case_type", "create_time", "update_time", "create_user", "update_user");
    private static final List<String> EXPECTATION_TEMPLATE_COLUMNS = ImmutableList.of("name", "granularity", "description", "converter", "display_parameters");


    private final DatabaseOperator databaseOperator;
    private final DataSourceClient dataSourceClient;

    @Inject
    public DataQualityClient(DatabaseOperator databaseOperator, DataSourceClient dataSourceClient) {
        this.databaseOperator = databaseOperator;
        this.dataSourceClient = dataSourceClient;
    }

    public Expectation findById(Long id) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(EXPECTATION_MODEL_NAME, EXPECTATION_COLUMNS);
        columnsMap.put(EXPECTATION_TEMPLATE_MODEL_NAME, EXPECTATION_TEMPLATE_COLUMNS);

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(EXPECTATION_TABLE_NAME, EXPECTATION_MODEL_NAME)
                .join("inner", EXPECTATION_TEMPLATE_TABLE_NAME, EXPECTATION_TEMPLATE_MODEL_NAME)
                .on(EXPECTATION_MODEL_NAME + ".template_name = " + EXPECTATION_TEMPLATE_MODEL_NAME + ".name")
                .where(EXPECTATION_MODEL_NAME + ".id = ?")
                .autoAliasColumns()
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            Expectation.Builder builder = Expectation.newBuilder();
            if (rs.next()) {
                ExpectationTemplate expectationTemplate = ExpectationTemplate.newBuilder()
                        .withName(rs.getString(EXPECTATION_TEMPLATE_MODEL_NAME + "_name"))
                        .withGranularity(rs.getString(EXPECTATION_TEMPLATE_MODEL_NAME + "_granularity"))
                        .withDescription(rs.getString(EXPECTATION_TEMPLATE_MODEL_NAME + "_description"))
                        .withConverter(rs.getString(EXPECTATION_TEMPLATE_MODEL_NAME + "_converter"))
                        .withDisplayParameters(rs.getString(EXPECTATION_TEMPLATE_MODEL_NAME + "_display_parameters"))
                        .build();
                Dataset dataset = buildDataset(rs);
                builder
                        .withExpectationId(rs.getLong(EXPECTATION_MODEL_NAME + "_id"))
                        .withName(rs.getString(EXPECTATION_MODEL_NAME + "_name"))
                        .withDescription(rs.getString(EXPECTATION_MODEL_NAME + "_description"))
                        .withGranularity(rs.getString(EXPECTATION_MODEL_NAME + "_granularity"))
                        .withTemplate(expectationTemplate)
                        .withPayload(JSONUtils.jsonStringToMap(rs.getString(EXPECTATION_MODEL_NAME + "_payload")))
                        .withTrigger(Expectation.ExpectationTrigger.valueOf(rs.getString(EXPECTATION_MODEL_NAME + "_trigger")))
                        .withTaskId(rs.getLong(EXPECTATION_MODEL_NAME + "_task_id"))
                        .withCaseType(CaseType.valueOf(rs.getString(EXPECTATION_MODEL_NAME + "_case_type")))
                        .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(EXPECTATION_MODEL_NAME + "_create_time")))
                        .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(EXPECTATION_MODEL_NAME + "_update_time")))
                        .withCreateUser(rs.getString(EXPECTATION_MODEL_NAME + "_create_user"))
                        .withUpdateUser(rs.getString(EXPECTATION_MODEL_NAME + "_update_user"));

                builder.withDataset(dataset);
            }
            return builder.build();
        }, id);
    }

    private Dataset buildDataset(ResultSet rs) throws SQLException {
        Long datasetGid = rs.getLong(EXPECTATION_MODEL_NAME + "_dataset_gid");
        Long dataSourceId = dataSourceClient.getDataSourceIdByGid(datasetGid);
        DataSource dataSourceById = dataSourceClient.getDataSourceById(dataSourceId);
        return Dataset.builder().gid(datasetGid).dataSource(dataSourceById).build();
    }

}
