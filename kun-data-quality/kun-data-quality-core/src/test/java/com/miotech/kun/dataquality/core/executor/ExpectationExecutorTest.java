package com.miotech.kun.dataquality.core.executor;

import com.google.common.collect.Maps;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.factory.MockExpectationFactory;
import com.miotech.kun.dataquality.core.factory.MockExpectationTemplateFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.PostgresConnectionConfigInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

public class ExpectationExecutorTest {

    public static final String POSTGRES_IMAGE = "postgres:12.3";
    protected PostgreSQLContainer postgresContainer;

    @BeforeEach
    public void setUp() {
        postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE).withInitScript("sql/init_postgresql.sql");
        postgresContainer.start();
    }

    @AfterEach
    public void tearDown() {
        postgresContainer.close();
    }

    @ParameterizedTest
    @MethodSource("expectedValues")
    public void testRun(String sql, String expectedValue, boolean expectedResult) {
        String field = "c";
        String comparisonOperator = "=";
        Integer comparisonPeriod = 0;
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("sql", sql);
        payload.put("field", field);
        payload.put("comparisonOperator", comparisonOperator);
        payload.put("expectedValue", expectedValue);
        payload.put("comparisonPeriod", comparisonPeriod);

        ExpectationTemplate expectationTemplate = MockExpectationTemplateFactory.create("com.miotech.kun.dataquality.core.converter.CustomSQLExpectationConverter");
        PostgresConnectionConfigInfo connectionInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, postgresContainer.getHost(), postgresContainer.getFirstMappedPort(), postgresContainer.getUsername(), postgresContainer.getPassword());
        Expectation expectation = MockExpectationFactory.createCustomSQLExpectation(expectationTemplate, payload, connectionInfo);

        MetricsCollectedResult<String> resultOfMock = new MetricsCollectedResult<>(null, DateTimeUtils.now(), expectedValue);

        ExpectationDatabaseOperator dbOperator = Mockito.mock(ExpectationDatabaseOperator.class);
        doReturn(resultOfMock).when(dbOperator).getTheResultCollectedNDaysAgo(anyLong(), anyInt());


        ExpectationExecutor executor = new ExpectationExecutor(expectation, dbOperator);

        boolean result = executor.execute();
        assertThat(result, is(expectedResult));
    }

    public static Stream<Arguments> expectedValues() {
        return Stream.of(
                Arguments.of("select count(1) c from bar", "3", true),
                Arguments.of("select count(1) c from bar_non_existent", "3", false)

        );

    }

}
