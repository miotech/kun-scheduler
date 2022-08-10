package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLUtils;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @program: kun
 * @description: filter
 * @author: zemin  huang
 * @create: 2022-08-08 17:35
 **/
@Singleton
public class FilterRuleDao {
    private final Logger logger = LoggerFactory.getLogger(FilterRuleDao.class);
    @Inject
    private DatabaseOperator dbOperator;


    private static final String TABLE_KMFR = "kun_mt_filter_rule";
    private static final String COLUMN_FILTER_TYPE = "filter_type";
    private static final String COLUMN_POSITIVE = "positive";
    private static final String COLUMN_RULE = "rule";
    private static final String COLUMN_UPDATED_TIME = "updated_time";
    private static final String COLUMN_DELETED = "deleted";
    private static final String[] COLUMNS = {COLUMN_FILTER_TYPE, COLUMN_POSITIVE, COLUMN_RULE, COLUMN_UPDATED_TIME, COLUMN_DELETED};


    public void save(FilterRule filterRule) {
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                filterRule.getType().name(),
                filterRule.getPositive(),
                StringUtils.stripToEmpty(filterRule.getRule()),
                DateTimeUtils.now(),
                false
        );
        String sql = DefaultSQLBuilder.newBuilder().insert(COLUMNS).into(TABLE_KMFR).asPrepared().getSQL();
        dbOperator.update(sql, params.toArray());
    }

    public void remove(FilterRuleType type, Boolean positive, String rule) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_FILTER_TYPE, COLUMN_POSITIVE, COLUMN_RULE);
        ImmutableList<String> set_options = ImmutableList.of(COLUMN_UPDATED_TIME, COLUMN_DELETED);
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                DateTimeUtils.now(),
                true,
                type.name(),
                positive,
                rule
        );
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KMFR)
                .set(SQLUtils.set(set_options))
                .where(SQLUtils.and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }

    public FilterRule get(FilterRuleType type, Boolean positive, String rule) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_FILTER_TYPE, COLUMN_POSITIVE, COLUMN_RULE, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMFR).where(SQLUtils.and(options)).getSQL();
        return dbOperator.fetchOne(sql, FilterRuleMapper.INSTANCE, type.name(), positive, rule, false);

    }

    public boolean judge(FilterRuleType type, Boolean positive, String value) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_FILTER_TYPE, COLUMN_POSITIVE, COLUMN_DELETED);
        String similarString = String.format(" and ? similar to %s", COLUMN_RULE);
        String sql = DefaultSQLBuilder.newBuilder().select("count(1)>0 as pass").from(TABLE_KMFR).where(SQLUtils.and(options) + similarString).getSQL();
        return dbOperator.fetchOne(sql, rs -> rs.getBoolean("pass"), type.name(), positive, false, value);
    }

    public Boolean judge(FilterRuleType type, String value) {
        boolean positive = judge(type, true, value);
        if (!positive) {
            return false;
        }
        return !judge(type, false, value);
    }

    public void remove(FilterRule filterRule) {
        FilterRuleType type = filterRule.getType();
        Boolean positive = filterRule.getPositive();
        String rule = filterRule.getRule();
        Preconditions.checkNotNull(filterRule);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(positive);
        Preconditions.checkArgument(StringUtils.isNotBlank(filterRule.getRule()), "rule is not empty");
        remove(type, positive, rule);
    }

    private static class FilterRuleMapper implements ResultSetMapper<FilterRule> {
        public static final ResultSetMapper<FilterRule> INSTANCE = new FilterRuleMapper();
        private final Logger logger = LoggerFactory.getLogger(FilterRuleDao.FilterRuleMapper.class);

        @Override
        public FilterRule map(ResultSet rs) throws SQLException {
            return FilterRule.FilterRuleBuilder.builder()
                    .withType(FilterRuleType.valueOf(rs.getString(COLUMN_FILTER_TYPE)))
                    .withPositive(rs.getBoolean(COLUMN_POSITIVE))
                    .withRule(rs.getString(COLUMN_RULE))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(COLUMN_UPDATED_TIME)))
                    .withDeleted(rs.getBoolean(COLUMN_DELETED))
                    .build();
        }
    }
}
