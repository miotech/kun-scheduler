package com.miotech.kun.commons.db.sql;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultSQLBuilder implements SQLBuilder {

    private List<String> insertColumns;
    private String targetName;
    private List<String[]> insertValues;
    private Integer valueSize;

    private List<String> updateColumns = new ArrayList<>();
    private String updateTargetName;
    private List<String> selectedFields;
    private Map<String, List<String>> columnsMap;
    private String fromTable;
    private String fromTableAlias;

    private List<String> joinTypes = new ArrayList<>();
    private List<String> joinTables = new ArrayList<>();
    private List<String> joinTableAliases = new ArrayList<>();
    private List<String> onClauses = new ArrayList<>();

    private String filterClause;
    private String groupByClause;
    private String havingClause;

    private boolean asPrepared = false;
    private boolean asPlain = false;
    private boolean autoAliasColumn = false;
    private boolean isDelete = false;
    private Integer limit;
    private boolean useLimit = false;
    private Integer offset;
    private boolean useOffset = false;
    private String[] orderByClause;
    private String duplicateKey;
    private String doFollowing;

    @Override
    public SQLBuilder insert(String... cols) {
        this.insertColumns = ImmutableList.copyOf(cols);
        this.valueSize = insertColumns.size();
        return this;
    }

    @Override
    public SQLBuilder into(String target) {
        this.targetName = target;
        return this;
    }

    @Override
    public SQLBuilder valueSize(Integer valueSize) {
        this.valueSize = valueSize;
        this.asPrepared = true;
        return this;
    }

    @Override
    public SQLBuilder delete() {
        isDelete = true;
        return this;
    }

    @Override
    public SQLBuilder update(String targetName) {
        this.updateTargetName = targetName;
        return this;
    }

    @Override
    public SQLBuilder set(String... cols) {
        this.updateColumns.addAll(Arrays.asList(cols));
        return this;
    }

    @Override
    public SQLBuilder values(String[]... values) {
        this.insertValues = ImmutableList.copyOf(values);
        return this;
    }

    @Override
    public SQLBuilder select(String... cols) {
        this.selectedFields = ImmutableList.copyOf(cols);
        return this;
    }

    @Override
    public SQLBuilder columns(Map<String, List<String>> columnsMap) {
        this.columnsMap = columnsMap;
        return this;
    }

    @Override
    public SQLBuilder from(String tableName) {
        return from(tableName, null);
    }

    @Override
    public SQLBuilder from(String tableName, String alias) {
        this.fromTable = tableName;
        this.fromTableAlias = alias;
        return this;
    }

    @Override
    public SQLBuilder join(String tableName, String alias) {
        return join(null, tableName, alias);
    }

    @Override
    public SQLBuilder join(String joinType, String tableName, String alias) {
        // Before inserting next join clause,
        // ensure that the last join clause should have an 'on' clause accompanied
        Preconditions.checkState(
                onClauses.size() == joinTables.size(),
                "The last join clause should have an accompanied 'on' clause before adding another join clause"
        );

        this.joinTypes.add(joinType);
        this.joinTables.add(tableName);
        this.joinTableAliases.add(alias);
        return this;
    }

    @Override
    public SQLBuilder on(String onClause) {
        this.onClauses.add(onClause);
        return this;
    }

    @Override
    public SQLBuilder where(String filterClause) {
        this.filterClause = filterClause;
        return this;
    }

    @Override
    public SQLBuilder limit() {
        this.useLimit = true;
        return this;
    }

    @Override
    public SQLBuilder limit(Integer limit) {
        this.limit = limit;
        this.useLimit = true;
        return this;
    }

    @Override
    public SQLBuilder offset() {
        this.useOffset = true;
        return this;
    }

    @Override
    public SQLBuilder offset(Integer offset) {
        this.offset = offset;
        this.useOffset = true;
        return this;
    }

    @Override
    public SQLBuilder orderBy(String... orderClause) {
        this.orderByClause = orderClause;
        return this;
    }

    @Override
    public SQLBuilder groupBy(String groupByClause) {
        this.groupByClause = groupByClause;
        return this;
    }

    @Override
    public SQLBuilder having(String havingClause) {
        this.havingClause = havingClause;
        return this;
    }

    @Override
    public SQLBuilder asPrepared() {
        this.asPrepared = true;
        return this;
    }

    @Override
    public SQLBuilder asPlain() {
        this.asPlain = true;
        return this;
    }

    @Override
    public SQLBuilder autoAliasColumns() {
        this.autoAliasColumn = true;
        return this;
    }

    @Override
    public SQLBuilder duplicateKey(String key, String doFollowing) {
        this.duplicateKey = key;
        this.doFollowing = doFollowing;
        return this;
    }

    @Override
    public String getSQL() {
        StringBuilder sqlBuilder = new StringBuilder();

        // build insert
        if (StringUtils.isNotBlank(targetName)) {
            sqlBuilder = buildInsert(sqlBuilder);
        }
        // build select
        if (CollectionUtils.isNotEmpty(selectedFields)
                || columnsMap != null && !columnsMap.isEmpty()) {
            sqlBuilder = buildSelect(sqlBuilder);
        }
        // build delete
        if (isDelete) {
            sqlBuilder.append("DELETE");
        }

        if (CollectionUtils.isNotEmpty(updateColumns)) {
            buildUpdate(sqlBuilder);
        }
        buildQuery(sqlBuilder);
        return sqlBuilder.toString();
    }

    public static SQLBuilder newBuilder() { return new DefaultSQLBuilder(); }

    private StringBuilder buildInsert(StringBuilder stringBuilder) {
        stringBuilder.append("INSERT INTO ").append(targetName);
        if (CollectionUtils.isNotEmpty(insertColumns)) {
            stringBuilder.append(" (");
            stringBuilder.append(String.join(", ", insertColumns));
            stringBuilder.append(") ");
        }
        stringBuilder.append("\n");
        if (CollectionUtils.isNotEmpty(insertValues)) {
            stringBuilder.append("VALUES ");
            String valueStr = insertValues.stream()
                    .map(x -> "(" + String.join(", ", x) + ")")
                    .collect(Collectors.joining(", "));
            stringBuilder.append(valueStr);
        } else if (valueSize > 0 && asPrepared) {
            stringBuilder.append("VALUES (");
            for (int i = 0; i< valueSize; i++) {
                stringBuilder.append("?");
                if (i != valueSize -1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(")");
        }

        if(StringUtils.isNotEmpty(this.duplicateKey)) {
            stringBuilder.append("\n ON CONFLICT (").append(this.duplicateKey).append(")");
            this.doFollowing = StringUtils.isNotEmpty(this.doFollowing) ? this.doFollowing : "DO NOTHING";
            this.doFollowing = this.doFollowing.startsWith("DO ") ? this.doFollowing : "DO " + this.doFollowing;
            stringBuilder.append("\n ").append(this.doFollowing);
        }
        return stringBuilder;
    }

    private StringBuilder buildUpdate(StringBuilder stringBuilder) {
        stringBuilder
                .append("UPDATE ")
                .append(updateTargetName);
        if (CollectionUtils.isNotEmpty(updateColumns)) {
            stringBuilder.append("\n");

            stringBuilder.append("SET ");
            if (asPrepared) {
                String updateClause = updateColumns.stream()
                        .map(x -> String.format("%s = ?", x))
                        .collect(Collectors.joining(", "));
                stringBuilder.append(updateClause);
            } else {
                stringBuilder.append(String.join(", ", updateColumns));
            }
        }
        return stringBuilder;
    }

    private StringBuilder buildSelect(StringBuilder stringBuilder) {
        if (CollectionUtils.isNotEmpty(selectedFields)) {
            if (!selectedFields.get(0).startsWith("SELECT")) {
                stringBuilder.append("SELECT ");
            }
            stringBuilder.append(String.join(", ", this.selectedFields));
        } else {
            stringBuilder.append("SELECT ");
            List<String> selectedColumns = columnsMap.entrySet()
                    .stream()
                    .flatMap(x -> x.getValue()
                            .stream()
                            .map(t -> {
                                String baseColumn = x.getKey() + "." + t;
                                if (autoAliasColumn) {
                                    baseColumn += " AS " + x.getKey() + "_" + t;
                                }
                                return baseColumn;
                            }))
                    .collect(Collectors.toList());
            stringBuilder.append(String.join(", ", selectedColumns));
        }

        return stringBuilder;
    }

    private StringBuilder buildQuery(StringBuilder stringBuilder) {

        if (StringUtils.isNotBlank(fromTable)) {
            stringBuilder.append("\n");
            stringBuilder.append("FROM ");
            stringBuilder.append(fromTable);
            if (StringUtils.isNotBlank(fromTableAlias)) {
                stringBuilder.append(" AS ");
                stringBuilder.append(fromTableAlias);
            }
        }

        if (!joinTables.isEmpty()) {
            for (int i = 0; i < joinTables.size(); i += 1) {
                stringBuilder.append("\n");
                if (StringUtils.isNotBlank(joinTypes.get(i))) {
                    stringBuilder.append(joinTypes.get(i)).append(" ");
                }
                stringBuilder.append("JOIN ");
                stringBuilder.append(joinTables.get(i));
                if (StringUtils.isNotBlank(joinTableAliases.get(i))) {
                    stringBuilder.append(" AS ");
                    stringBuilder.append(joinTableAliases.get(i));
                }
                if ((i < onClauses.size()) && StringUtils.isNotBlank(onClauses.get(i))) {
                    stringBuilder.append("\n");
                    stringBuilder.append("ON ");
                    stringBuilder.append(onClauses.get(i));
                }
            }
        }

        if (StringUtils.isNotBlank(filterClause)) {
            stringBuilder.append("\n");
            stringBuilder.append("WHERE ");
            stringBuilder.append(filterClause);
        }

        if (StringUtils.isNotBlank(groupByClause)) {
            stringBuilder.append("\n");
            stringBuilder.append("GROUP BY ");
            stringBuilder.append(groupByClause);
        }

        if (StringUtils.isNotBlank(havingClause)) {
            stringBuilder.append("\n");
            stringBuilder.append("HAVING ");
            stringBuilder.append(havingClause);
        }

        if (ArrayUtils.isNotEmpty(orderByClause)) {
            stringBuilder.append("\n");
            stringBuilder.append("ORDER BY ");
            stringBuilder.append(String.join(", ", orderByClause));
        }

        if (limit != null && limit >=0 || useLimit) {
            stringBuilder.append("\n");
            stringBuilder.append("LIMIT ");
            if (asPrepared && useLimit) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append(limit);
            }
        }
        if (offset != null && offset >=0  || useOffset) {
            stringBuilder.append("\n");
            stringBuilder.append("OFFSET ");
            if (asPrepared && useOffset) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append(offset);
            }
        }
        return stringBuilder;
    }
}
