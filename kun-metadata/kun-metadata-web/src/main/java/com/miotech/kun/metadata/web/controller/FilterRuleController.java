package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.FilterRuleService;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-09 15:27
 **/
@Singleton
public class FilterRuleController {
    @Inject
    private FilterRuleService filterRuleService;
    private static final Logger logger = LoggerFactory.getLogger(FilterRuleController.class);

    @RouteMapping(url = "/filter_rule/add", method = "POST")
    public void createFilterRule(@RequestBody FilterRule filterRule) {
        filterRuleService.addFilterRule(filterRule);
    }

    @RouteMapping(url = "/filter_rule/remove", method = "POST")
    public void removeFilterRule(@RequestBody FilterRule filterRule) {
        filterRuleService.removeFilterRule(filterRule);
    }

    @RouteMapping(url = "/filter_rule/judge", method = "GET")
    public Boolean judge(@QueryParameter FilterRuleType type, @QueryParameter String value) {
        return filterRuleService.judge(type, value);
    }

}
