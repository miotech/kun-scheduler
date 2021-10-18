package com.miotech.kun.metadata.common.service;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataSourceServiceTest extends DatabaseTestBase {

    @Inject
    private DataSourceDao dataSourceDao;

    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void init(){
        databaseOperator.update("delete from kun_mt_datasource_type");
        databaseOperator.update("INSERT INTO kun_mt_datasource_type (id, name)\n" +
                "VALUES (1, 'Hive'),\n" +
                "       (2, 'MongoDB'),\n" +
                "       (3, 'PostgreSQL'),\n" +
                "       (4, 'Elasticsearch'),\n" +
                "       (5, 'Arango'),\n" +
                "       (6, 'AWS')\n" +
                ";");
    }

    @Test
    public void createDataSourceHasExist_should_throw_IllegalArgumentException() {
        //prepare
        Map<String,Object> values = new HashMap<>();
        values.put("host","127.0.0.1");
        values.put("port","5432");
        DataSource saved = MockDataSourceFactory.createDataSource(1,"saved",values,3,new ArrayList<>());
        dataSourceDao.create(saved);
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("new",values,3,new ArrayList<>());

        //verify
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("datasource with type " + dataSourceRequest.getTypeId()
                +  " and connection info " + dataSourceRequest.getConnectionInfo() +" is exist");
        dataSourceService.create(dataSourceRequest);

    }

    @Test
    public void getDataSourceIdByConnectionInfo(){
        //prepare
        Map<String,Object> values = new HashMap<>();
        values.put("host","127.0.0.1");
        values.put("port","5432");
        DataSource saved = MockDataSourceFactory.createDataSource(1,"saved",values,3,new ArrayList<>());
        dataSourceDao.create(saved);

        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.POSTGRES_TABLE,new ConnectionInfo(values));
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        //verify
        assertThat(selected.getConnectionInfo().getValues(),hasEntry("host","127.0.0.1"));
        assertThat(selected.getConnectionInfo().getValues(),hasEntry("port","5432"));
        assertThat(selected.getId(),is(dataSourceId));
        assertThat(selected.getTypeId(),is(saved.getTypeId()));
    }

    @Test
    public void getDataSourceIdByHiveStore_should_priority_to_return_hive_source(){
        //prepare
        Map<String,Object> values = new HashMap<>();
        DataSource aws = MockDataSourceFactory.createDataSource(1,"aws",values,6,new ArrayList<>());
        dataSourceDao.create(aws);
        DataSource hive = MockDataSourceFactory.createDataSource(2,"hive",values,1,new ArrayList<>());
        dataSourceDao.create(hive);

        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.HIVE_TABLE
                ,new ConnectionInfo(values));
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        //verify
        assertThat(selected.getId(),is(dataSourceId));
        assertThat(selected.getTypeId(),is(hive.getTypeId()));
        assertThat(selected.getName(),is(hive.getName()));
    }

    @Test
    public void getDataSourceIdByHiveStoreHiveSourceNotExist_should_return_aws_source(){
        //prepare
        Map<String,Object> values = new HashMap<>();
        DataSource aws = MockDataSourceFactory.createDataSource(1,"aws",values,6,new ArrayList<>());
        dataSourceDao.create(aws);

        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(DataStoreType.HIVE_TABLE
                ,new ConnectionInfo(values));
        DataSource selected = dataSourceDao.findById(dataSourceId).get();

        //verify
        assertThat(selected.getId(),is(dataSourceId));
        assertThat(selected.getTypeId(),is(aws.getTypeId()));
        assertThat(selected.getName(),is(aws.getName()));
    }

}
