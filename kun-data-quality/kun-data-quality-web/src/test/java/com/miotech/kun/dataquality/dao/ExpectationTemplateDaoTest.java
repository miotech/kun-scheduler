package com.miotech.kun.dataquality.dao;

import com.miotech.kun.dataquality.DataQualityTestBase;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.mock.MockExpectationTemplateFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationTemplateDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ExpectationTemplateDaoTest extends DataQualityTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExpectationTemplateDao expectationTemplateDao;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("truncate table kun_dq_expectation_template");
    }

    @Test
    public void testCreateThenFetch() {
        String collectMethod = "CUSTOM_SQL";
        String converter = "com.miotech.kun.dataquality.web.converter.SQLMetricsConverter";
        ExpectationTemplate expectationTemplate = MockExpectationTemplateFactory.create(collectMethod, Metrics.Granularity.CUSTOM.name(), null);
        expectationTemplateDao.create(expectationTemplate);

        ExpectationTemplate fetched = expectationTemplateDao.fetchByName(collectMethod);
        assertThat(fetched, sameBeanAs(expectationTemplate).ignoring("displayParameters"));
    }

    @Test
    public void testFetchByName_empty() {
        String collectMethod = "CUSTOM_SQL";

        ExpectationTemplate fetched = expectationTemplateDao.fetchByName(collectMethod);
        assertThat(fetched, nullValue());
    }

    @Test
    public void testFetchByGranularity() {
        String granularity = "CUSTOM";
        String collectMethod1 = "CUSTOM_SQL1";
        String collectMethod2 = "CUSTOM_SQL2";
        String converter1 = "com.miotech.kun.dataquality.web.converter.SQLMetricsConverter1";
        String converter2 = "com.miotech.kun.dataquality.web.converter.SQLMetricsConverter2";
        ExpectationTemplate expectationTemplate1 = MockExpectationTemplateFactory.create(collectMethod1, granularity, converter1);
        ExpectationTemplate expectationTemplate2 = MockExpectationTemplateFactory.create(collectMethod2, granularity, converter2);
        expectationTemplateDao.create(expectationTemplate1);
        expectationTemplateDao.create(expectationTemplate2);

        List<ExpectationTemplate> fetched = expectationTemplateDao.fetchByGranularity(granularity);
        assertThat(fetched.size(), is(2));
        List<String> converters = fetched.stream().map(ExpectationTemplate::getConverter).collect(Collectors.toList());
        assertThat(converters, containsInAnyOrder(converter1, converter2));
    }

}
