package com.miotech.kun.workflow.db.sql;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface SQLBuilder {

    SQLBuilder insert(String ...cols);

    SQLBuilder into(String target);

    SQLBuilder values(String[] ...values);

    SQLBuilder valueSize(Integer valueSize);

    SQLBuilder delete();

    SQLBuilder update(String target);

    SQLBuilder set(String ...cols);

    SQLBuilder select(String ...cols);

    SQLBuilder columns(Map<String, List<String>> columnsMap);

    SQLBuilder from (String tableName);

    SQLBuilder from (String tableName, String alias);

    SQLBuilder join (String tableName, String alias);

    SQLBuilder join (String joinType, String tableName, String alias);

    SQLBuilder on (String onClause);

    SQLBuilder where(String filterClause);

    SQLBuilder limit();

    SQLBuilder limit(Integer limit);

    SQLBuilder offset();

    SQLBuilder offset(Integer offset);

    SQLBuilder orderBy(String ...orderClause);

    SQLBuilder groupBy(String groupByClause);

    SQLBuilder having(String havingClause);

    SQLBuilder asPrepared();

    SQLBuilder asPlain();

    SQLBuilder autoAliasColumns();

    String getSQL();
}
