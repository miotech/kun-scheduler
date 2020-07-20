package com.miotech.kun.commons.query;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.query.datasource.DataSourceContainer;
import com.miotech.kun.commons.query.model.QueryResultSet;
import com.miotech.kun.commons.query.service.ConfigService;

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

    public QueryResultSet execute(JDBCQuery query) {
        DataSource dataSource = dataSourceContainer.getCacheDataSource(query.getQuerySite());
        DatabaseOperator databaseOperator = new DatabaseOperator(dataSource);
        return databaseOperator.query(query.getQueryEntry().getQueryString(), rs -> {
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
}
