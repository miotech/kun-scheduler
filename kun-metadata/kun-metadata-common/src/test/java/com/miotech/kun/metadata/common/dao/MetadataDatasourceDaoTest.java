package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.dto.DataSourceConnectionDTO;
import com.miotech.kun.metadata.core.model.dto.DataSourceDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class MetadataDatasourceDaoTest extends DatabaseTestBase {
    @Inject
    private MetadataDatasourceDao metadataDatasourceDao;

    @Inject
    private DatabaseOperator dbOperator;

    @Before
    public void init() {
        dbOperator.update(
                "INSERT INTO kun_mt_datasource(id, connection_info, type_id) VALUES(?, CAST(? AS JSONB), ?)",
                1L,
                "{\"host\": \"127.0.0.1\", \"port\": \"80\", \"password\": \"123456\", \"username\": \"root\"}",
                1L
        );
        dbOperator.update(
                "INSERT INTO kun_mt_datasource_type(id, name) VALUES(?, ?)",
                1L,
                "MongoDB"
        );
    }

    @Test
    public void testGetDataSourceById_exist() {
        // Execute
        DataSourceDTO dataSource = metadataDatasourceDao.getDataSourceById(1L);

        // Validate
        assertThat(dataSource, notNullValue());
        assertThat(dataSource.getId(), is(1L));
        assertThat(dataSource.getTypeId(), is(1L));
        assertThat(dataSource.getType(), is("MongoDB"));
        assertTrue(EqualsBuilder.reflectionEquals(dataSource.getConnectionInfo(), DataSourceConnectionDTO.newBuilder()
                .withHost("127.0.0.1")
                .withPort("80")
                .withUsername("root")
                .withPassword("123456")
                .build()));
    }

    @Test
    public void testGetDataSourceById_notExist() {
        // Execute
        DataSourceDTO dataSource = metadataDatasourceDao.getDataSourceById(2L);

        // Validate
        assertThat(dataSource, nullValue());
    }

}