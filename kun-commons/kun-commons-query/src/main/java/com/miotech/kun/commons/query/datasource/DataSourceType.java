package com.miotech.kun.commons.query.datasource;

import com.miotech.kun.commons.query.model.JDBCConnectionInfo;
import com.miotech.kun.commons.query.model.MetadataConnectionInfo;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

/**
 * @author: Jie Chen
 * @created: 2020/7/10
 */
public enum DataSourceType {

    PostgreSQL("postgresql", "org.postgresql.Driver"),

    AWS("awsathena", "com.simba.athena.jdbc.Driver");

    private static final String PROTOCOL_DELIMITER = ":";

    private static final String PATH_SEPARATOR = "/";

    private static final String HEADER_DELIMITER = "//";

    private static final String JDBC_PROTOCOL = "jdbc" + PROTOCOL_DELIMITER;

    private String protocalHeader;

    private String driverClass;

    DataSourceType(String protocalHeader,
                   String driverClass) {
        this.protocalHeader = protocalHeader;
        this.driverClass = driverClass;
    }

    public static JDBCConnectionInfo getJDBCConnectionInfo(MetadataConnectionInfo mcInfo) {
        if (StringUtils.isEmpty(mcInfo.getType())) {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException("DataSource type is empty, unable to parse url"));
        }

        if (StringUtils.equals(PostgreSQL.name(), mcInfo.getType())) {
            return parseFormalConnectionInfo(PostgreSQL, mcInfo);

        } else if (StringUtils.equals(AWS.name(), mcInfo.getType())) {
            JDBCConnectionInfo connectionInfo = new JDBCConnectionInfo();
            connectionInfo.setUrl((String) mcInfo.getConnectionInfo().get("athenaUrl"));
            connectionInfo.setUsername((String) mcInfo.getConnectionInfo().get("athenaUsername"));
            connectionInfo.setPassword((String) mcInfo.getConnectionInfo().get("athenaPassword"));
            connectionInfo.setDriverClass(AWS.getDriverClass());
            return connectionInfo;
        } else {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException("Unsupported datasource type: " + mcInfo.getType()));
        }
    }

    public String getProtocalHeader() {
        return protocalHeader;
    }

    public String getDriverClass() {
        return driverClass;
    }

    private static JDBCConnectionInfo parseFormalConnectionInfo(DataSourceType dataSourceType,
                                                                MetadataConnectionInfo mcInfo) {
        JSONObject rawValue = mcInfo.getConnectionInfo();

        String url = parseFormalURL(dataSourceType.getProtocalHeader(), (String) rawValue.get("host"), ((Long) rawValue.get("port")).intValue(), mcInfo.getUrlPostfix());
        String username = (String) rawValue.get("username");
        String password = (String) rawValue.get("password");
        JDBCConnectionInfo connectionInfo = new JDBCConnectionInfo();
        connectionInfo.setUrl(url);
        connectionInfo.setUsername(username);
        connectionInfo.setPassword(password);
        connectionInfo.setDriverClass(dataSourceType.getDriverClass());
        return connectionInfo;
    }

    private static String parseFormalURL(String protocalHeader,
                                         String host,
                                         Integer port,
                                         String databaseName) {
        return JDBC_PROTOCOL
                + protocalHeader
                + PROTOCOL_DELIMITER
                + HEADER_DELIMITER
                + host
                + PROTOCOL_DELIMITER
                + port
                + PATH_SEPARATOR
                + databaseName;
    }
}
