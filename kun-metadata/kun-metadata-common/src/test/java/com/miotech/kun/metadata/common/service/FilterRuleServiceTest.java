package com.miotech.kun.metadata.common.service;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @program: kun
 * @description: filter
 * @author: zemin  huang
 * @create: 2022-08-08 17:35
 **/
public class FilterRuleServiceTest extends DatabaseTestBase {

    @Inject
    private FilterRuleService filterRuleService;


    @Test
    public void add_filter_rule_test() {
        FilterRuleType type = FilterRuleType.EMPTY;
        Boolean positive = true;
        String rule = "/lev/lev2/lev3";
        FilterRule filterRule = mockFilterRule(type, positive, rule);
        filterRuleService.addFilterRule(filterRule);
        FilterRule filterRuleResult = filterRuleService.get(filterRule);
        assertThat(filterRuleResult.getDeleted(), is(false));
        assertThat(filterRuleResult.getType(), is(type));
        assertThat(filterRuleResult.getPositive(), is(positive));
        assertThat(filterRuleResult.getRule(), is(rule));


    }

    @NotNull
    private FilterRule mockFilterRule(FilterRuleType type, Boolean positive, String rule) {
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder()
                .withType(type)
                .withPositive(positive)
                .withRule(rule).build();
        return filterRule;
    }

    @Test
    public void filter_rule_test_mce() {
        FilterRuleType type = FilterRuleType.MCE;
        Boolean positive = true;
        String mceRule1 = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule1 = mockFilterRule(type, positive, mceRule1);
        filterRuleService.addFilterRule(filterRule1);
        String value1 = FilterRuleType.mceRule("hive", "test_datasource", "test_database", "test_table");
        assertThat(filterRuleService.judge(FilterRuleType.MCE, value1), is(true));
        filterRuleService.removeFilterRule(filterRule1);

        String mceRule2 = FilterRuleType.mceRule("hive", "%", "%", "%");
        FilterRule filterRule2 = mockFilterRule(type, positive, mceRule2);
        filterRuleService.addFilterRule(filterRule2);
        String value2 = FilterRuleType.mceRule("hive", "test_datasource", "test_database", "test_table");
        assertThat(filterRuleService.judge(FilterRuleType.MCE, value2), is(true));
        filterRuleService.removeFilterRule(filterRule2);

        String mceRule3 = FilterRuleType.mceRule("test", "%", "%", "%");
        FilterRule filterRule3 = mockFilterRule(type, positive, mceRule3);
        filterRuleService.addFilterRule(filterRule3);
        String value3 = FilterRuleType.mceRule("hive", "test_datasource", "test_database", "test_table");
        assertThat(filterRuleService.judge(FilterRuleType.MCE, value3), is(false));
        filterRuleService.removeFilterRule(filterRule3);

        String mceRule4 = FilterRuleType.mceRule("hive", "%", "test", "%");
        FilterRule filterRule4 = mockFilterRule(type, positive, mceRule4);
        filterRuleService.addFilterRule(filterRule4);
        String value4 = FilterRuleType.mceRule("hive", "test_datasource", "test_database", "test_table");
        assertThat(filterRuleService.judge(FilterRuleType.MCE, value4), is(false));
    }

    @Test
    public void remove_filter_rule_test() {
        FilterRuleType type = FilterRuleType.EMPTY;
        Boolean positive = true;
        String rule = "/lev/lev2/lev3";
        FilterRule filterRule = mockFilterRule(type, positive, rule);
        filterRuleService.addFilterRule(filterRule);
        filterRuleService.removeFilterRule(filterRule);
        FilterRule filterRuleResult = filterRuleService.get(filterRule);
        assertThat(filterRuleResult, is(nullValue()));
    }

    @Test
    public void judge_positive() {
        FilterRuleType type = FilterRuleType.EMPTY;
        filterRuleService.addFilterRule(mockFilterRule(type, true, "/test_1/test_2/%"));
        assertThat(filterRuleService.judge(type, ("/")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1")), is(false));
        assertThat(filterRuleService.judge(type, ("/test")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_3")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_3/test_4")), is(true));
    }

    @Test
    public void judge() {
        FilterRuleType type = FilterRuleType.EMPTY;
        filterRuleService.addFilterRule(mockFilterRule(type, true, "/test_1/test_2/%"));
        filterRuleService.addFilterRule(mockFilterRule(type, false, "/test_1/test_2/test_4/%"));
        assertThat(filterRuleService.judge(type, ("/")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1")), is(false));
        assertThat(filterRuleService.judge(type, ("/test")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_3")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_3/test_4")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_4/")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_4/test_5")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/test_4/test_6")), is(false));
    }

    @Test
    public void judge_addBlackList() {
        FilterRuleType type = FilterRuleType.EMPTY;
        filterRuleService.addFilterRule(mockFilterRule(type, true, "%"));
        assertThat(filterRuleService.judge(type, ("test_1")), is(true));
        assertThat(filterRuleService.judge(type, ("/")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1")), is(true));
        assertThat(filterRuleService.judge(type, ("/test")), is(true));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/")), is(true));
        filterRuleService.addFilterRule(mockFilterRule(type, false, "%"));
        assertThat(filterRuleService.judge(type, ("/")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1")), is(false));
        assertThat(filterRuleService.judge(type, ("/test")), is(false));
        assertThat(filterRuleService.judge(type, ("/test_1/test_2/")), is(false));

    }


}
