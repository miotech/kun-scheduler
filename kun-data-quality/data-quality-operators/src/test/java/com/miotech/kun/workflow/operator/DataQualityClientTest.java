package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.operator.client.DataSourceClient;
import com.miotech.kun.workflow.operator.mock.MockExpectationFactory;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.INFRA_BASE_URL;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;

public class DataQualityClientTest extends DatabaseTestBase {

    private static final String EXPECTATION_TABLE_NAME = "kun_dq_expectation";
    private static final List<String> EXPECTATION_COLUMNS = ImmutableList.of("id", "name", "types", "description", "granularity",
            "template_name", "payload", "trigger", "dataset_gid", "task_id", "case_type", "create_time", "update_time", "create_user", "update_user");
    @Inject
    private DataQualityClient dataQualityClient;

    @Inject
    private DataSourceClient dataSourceClient;

    @Inject
    private DatabaseOperator databaseOperator;

    @Override
    protected void configuration() {
        super.configuration();
        setFlywayLocation("webapp_sql");
        Props props = new Props();
        props.put(INFRA_BASE_URL, "http://kun-infra");
        bind(Props.class, props);
        bind(DataSourceClient.class, Mockito.mock(DataSourceClient.class));
    }

    @Test
    public void testFindById_empty() {
        long id = IdGenerator.getInstance().nextId();
        Expectation expectation = dataQualityClient.findById(id);
        assertThat(expectation.getExpectationId(), nullValue());
        assertThat(expectation.getName(), nullValue());
        assertThat(expectation.getTypes(), nullValue());
        assertThat(expectation.getDescription(), nullValue());
        assertThat(expectation.getTrigger(), nullValue());
        assertThat(expectation.getTaskId(), nullValue());
    }

    @Test
    public void testFindById_createThenFind() {
        Expectation expectation = MockExpectationFactory.create();
        createExpectation(expectation);

        Long dataSourceId = IdGenerator.getInstance().nextId();
        DataSource dataSource = DataSource.newBuilder().withId(dataSourceId).build();
        doReturn(dataSourceId).when(dataSourceClient).getDataSourceIdByGid(expectation.getDataset().getGid());
        doReturn(dataSource).when(dataSourceClient).getDataSourceById(dataSourceId);

        Expectation fetched = dataQualityClient.findById(expectation.getExpectationId());
        assertThat(expectation, sameBeanAs(fetched).ignoring("dataset").ignoring("template").ignoring("payload"));
        assertThat(expectation.getDataset().getGid(), is(fetched.getDataset().getGid()));
    }

    private void createExpectation(Expectation expectation) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        databaseOperator.update(sql,
                expectation.getExpectationId(),
                expectation.getName(),
                StringUtils.join(expectation.getTypes(), ","),
                expectation.getDescription(),
                expectation.getGranularity(),
                expectation.getTemplate().getName(),
                JSONUtils.toJsonString(expectation.getPayload()),
                expectation.getTrigger().name(),
                expectation.getDataset().getGid(),
                expectation.getTaskId(),
                expectation.getCaseType().name(),
                expectation.getCreateTime(),
                expectation.getUpdateTime(),
                expectation.getCreateUser(),
                expectation.getUpdateUser());
    }

}
