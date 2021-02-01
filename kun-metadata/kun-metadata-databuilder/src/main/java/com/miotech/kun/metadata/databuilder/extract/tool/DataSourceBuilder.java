package com.miotech.kun.metadata.databuilder.extract.tool;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.model.*;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;

@Singleton
public class DataSourceBuilder {

    private final DatabaseOperator operator;

    @Inject
    public DataSourceBuilder(DatabaseOperator operator) {
        this.operator = operator;
    }

    public DataSource fetchById(long id) {
        String sql = "SELECT kmds.id, kmdt.name, kmds.connection_info FROM kun_mt_datasource kmds JOIN kun_mt_datasource_type kmdt ON kmds.type_id = kmdt.id WHERE kmds.id = ?";
        return operator.fetchOne(sql, rs -> generateDataSource(rs.getLong(1), rs.getString(2), rs.getString(3)), id);
    }

    public DataSource fetchByGid(Long gid) {
        String sql = "SELECT kmds.id, kmdt.name, kmds.connection_info FROM kun_mt_dataset kmd JOIN kun_mt_datasource kmds ON kmd.datasource_id = kmds.id JOIN kun_mt_datasource_type kmdt ON kmds.type_id = kmdt.id WHERE kmd.gid = ?";
        return operator.fetchOne(sql, rs -> generateDataSource(rs.getLong(1), rs.getString(2), rs.getString(3)), gid);
    }

    private DataSource generateDataSource(long id, String datasourceType, String connStr) {
        DataSource.Type type = DataSource.Type.valueOf(datasourceType.toUpperCase());

        if (type.equals(DataSource.Type.AWS)) {
            AWSDataSource awsConnection = JSONUtils.jsonToObject(connStr, AWSDataSource.class);
            return AWSDataSource.clone(awsConnection).withId(id).build();
        }

        JDBCConnection jdbcConnection = JSONUtils.jsonToObject(connStr, JDBCConnection.class);
        switch (type) {
            case POSTGRESQL:
                PostgresDataSource.Builder postgresDataSourceBuilder = PostgresDataSource.newBuilder();
                postgresDataSourceBuilder.withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.POSTGRES))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword());
                return postgresDataSourceBuilder.build();
            case MONGODB:
                MongoDataSource.Builder mongoDataSourceBuilder = MongoDataSource.newBuilder();
                mongoDataSourceBuilder.withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.MONGO))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword());
                return mongoDataSourceBuilder.build();
            case ELASTICSEARCH:
                ElasticSearchDataSource elasticSearchDataSource = ElasticSearchDataSource.newBuilder()
                        .withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.ELASTICSEARCH))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword())
                        .build();
                return elasticSearchDataSource;
            case ARANGO:
                ArangoDataSource arangoDataSource = ArangoDataSource.newBuilder()
                        .withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.ARANGO))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword())
                        .build();
                return arangoDataSource;
            default:
                throw new UnsupportedOperationException("Invalid datasource type: " + type);
        }
    }
}
