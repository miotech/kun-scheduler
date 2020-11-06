package com.miotech.kun.commons.query;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.query.datasource.DataSourceContainer;
import com.miotech.kun.commons.query.model.QueryResultSet;
import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.commons.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/7
 */
public class JDBCQueryExecutor {

    private DataSourceContainer dataSourceContainer;

    private JDBCQueryExecutor() {
        dataSourceContainer = DataSourceContainer.getInstance();
    }

    private static class SingletonHolder {
        private static JDBCQueryExecutor instance = new JDBCQueryExecutor();
    }

    public static JDBCQueryExecutor getInstance() {
        return SingletonHolder.instance;
    }

    public static JDBCQueryExecutor getInstance(String confName) {
        ConfigService.getInstance().loadConf(confName);
        return SingletonHolder.instance;
    }

    private void checkQuery(JDBCQuery query) {
        Preconditions.checkNotNull(query.getQueryEntry(), "QueryEntry is empty.");
        Preconditions.checkNotNull(query.getQuerySite(), "QuerySite is empty.");
        Preconditions.checkArgument(IdUtils.isNotEmpty(query.getQuerySite().getDatasetId())
                || IdUtils.isNotEmpty(query.getQuerySite().getDatasourceId()), "QuerySite is invalid, both dataset id and datasource id are empty");
    }

    public QueryResultSet execute(JDBCQuery query) {
        checkQuery(query);
        DataSource dataSource = dataSourceContainer.getCacheDataSource(query.getQuerySite());
        DatabaseOperator databaseOperator = new DatabaseOperator(dataSource);
        String queryString = handleQueryString(query.getQueryEntry().getQueryString());
        return databaseOperator.query(queryString, rs -> {
            QueryResultSet queryResultSet = new QueryResultSet();
            int colCount = rs.getMetaData().getColumnCount();
            List<String> colNames = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                String colName = rs.getMetaData().getColumnLabel(i);
                colNames.add(colName);
            }
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (String colName : colNames) {
                    row.put(colName, rs.getObject(colName));
                }
                queryResultSet.addRow(row);
            }
            return queryResultSet;
        }, query.getQueryEntry().getQueryArgsArray());
    }

    private String handleQueryString(String query) {
        if (StringUtils.isNotEmpty(query)) {
            query = query.trim();
            query = StringUtils.removeEnd(query, ";").trim();
        }
        return query;
    }
}
