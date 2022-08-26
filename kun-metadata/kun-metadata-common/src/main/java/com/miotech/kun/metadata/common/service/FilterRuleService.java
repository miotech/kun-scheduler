package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.FilterRuleDao;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @program: kun
 * @description: filter
 * @author: zemin  huang
 * @create: 2022-08-08 17:35
 **/
@Singleton
public class FilterRuleService {

    private final Logger logger = LoggerFactory.getLogger(FilterRuleService.class);
    private FilterRuleDao filterRuleDao;

    @Inject
    public FilterRuleService(FilterRuleDao filterRuleDao) {
        this.filterRuleDao = filterRuleDao;
    }

    public void addFilterRule(FilterRule filterRule) {
        if (Objects.nonNull(get(filterRule))) {
            return;
        }
        filterRuleDao.save(filterRule);
    }


    public void removeFilterRule(FilterRule filterRule) {
        filterRuleDao.remove(filterRule);
    }

    public Boolean judge(FilterRuleType type, String value) {
        return filterRuleDao.judge(type, value);
    }

    public FilterRule get(FilterRule filterRule) {
        return filterRuleDao.get(filterRule.getType(), filterRule.getPositive(), filterRule.getRule());
    }


}
