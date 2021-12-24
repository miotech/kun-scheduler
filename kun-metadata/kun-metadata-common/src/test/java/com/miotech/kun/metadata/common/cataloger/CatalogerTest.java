package com.miotech.kun.metadata.common.cataloger;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.client.ClientFactory;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(Theories.class)
public class CatalogerTest extends DatabaseTestBase {

    @Inject
    private FieldMappingService fieldMappingService;
    @Inject
    private DataSourceDao dataSourceDao;


    @DataPoints("ConnectionConfig")
    public static ConnectionConfig[] connectionConfigs() {
        ConnectionInfo athenaConnectionInfo = new AthenaConnectionInfo(ConnectionType.ATHENA,"jdbc:awsathena","user","password");
        ConnectionInfo glueConnectionInfo = new GlueConnectionInfo(ConnectionType.GLUE,"glue","glue","glue");
        ConnectionInfo s3ConnectionInfo = new S3ConnectionInfo(ConnectionType.S3,"glue","glue","glue");

        ConnectionConfig awsConfig = ConnectionConfig.newBuilder()
                .withUserConnection(athenaConnectionInfo)
                .withMetadataConnection(glueConnectionInfo)
                .withStorageConnection(s3ConnectionInfo)
                .build();

        ConnectionInfo hiveConnectionInfo = new HiveServerConnectionInfo(ConnectionType.HIVE_SERVER,"host",10000,"user","password");
        ConnectionInfo metastoreConnectionInfo = new HiveMetaStoreConnectionInfo(ConnectionType.HIVE_THRIFT,"url");

        ConnectionConfig hiveConfig = ConnectionConfig.newBuilder()
                .withUserConnection(hiveConnectionInfo)
                .withMetadataConnection(metastoreConnectionInfo)
                .build();

        ConnectionInfo pgConnectionInfo = new PostgresConnectionInfo(ConnectionType.POSTGRESQL,"127.0.0.1",5432);

        ConnectionConfig pgConfig = ConnectionConfig.newBuilder()
                .withUserConnection(pgConnectionInfo)
                .build();
        ConnectionConfig[] connectionConfigs = {awsConfig,hiveConfig,pgConfig};
        return connectionConfigs;
    }

    @Theory
    public void testGenerateCataloger(@FromDataPoints("ConnectionConfig") ConnectionConfig connectionConfig){
        ClientFactory clientFactory = mock(ClientFactory.class);
        CatalogerFactory catalogerFactory = new CatalogerFactory(fieldMappingService,clientFactory);

        DataSource aws = MockDataSourceFactory.createDataSource(1,"aws",connectionConfig,DatasourceType.HIVE,new ArrayList<>());
        dataSourceDao.create(aws);
        Cataloger cataloger = catalogerFactory.generateCataloger(aws);
        assertThat(cataloger,notNullValue());
    }

    class QueryResult{
        private ResultSet resultSet;
        private Connection connection;

        public ResultSet getResultSet() {
            return resultSet;
        }

        public void close(){
            try {
                connection.close();
            }catch (Exception e){

            }

        }
    }
}
