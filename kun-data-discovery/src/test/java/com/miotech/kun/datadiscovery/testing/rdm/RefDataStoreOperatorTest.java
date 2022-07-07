package com.miotech.kun.datadiscovery.testing.rdm;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.service.rdm.data.RefDataStoreOperator;
import com.miotech.kun.datadiscovery.testing.DataDiscoveryTestBase;
import com.miotech.kun.datadiscovery.testing.mockdata.MockRefDataVersionBasicFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-21 14:30
 **/

@Slf4j
public class RefDataStoreOperatorTest extends DataDiscoveryTestBase {
    @Autowired
    private AWSGlue awsGlue;
    @Autowired
    private RefDataStoreOperator refDataStoreOperator;

    @Test
    public void test_overwrite() {
        RefTableVersionInfo refTableVersionInfo = MockRefDataVersionBasicFactory.mockDefaultPublishedRefTableVersionInfo(1L, 1L, 1, "test_table");
        Mockito.doThrow(EntityNotFoundException.class).when(awsGlue).getTable(Mockito.any());
        Mockito.when(awsGlue.createTable(Mockito.any())).thenReturn(new CreateTableResult());
        AmazonWebServiceResult webServiceResult1 = refDataStoreOperator.overwrite(refTableVersionInfo);
        assertThat(webServiceResult1.getClass(), is(CreateTableResult.class));
        Mockito.doReturn(new GetTableResult()).when(awsGlue).getTable(Mockito.any());
        Mockito.when(awsGlue.updateTable(Mockito.any())).thenReturn(new UpdateTableResult());
        AmazonWebServiceResult webServiceResult2 = refDataStoreOperator.overwrite(refTableVersionInfo);
        assertThat(webServiceResult2.getClass(), is(UpdateTableResult.class));

    }

    @Test
    public void remove() {
        String databaseName = "test_database";
        String tableName = "test_table";
        Mockito.when(awsGlue.deleteTable(Mockito.any())).thenReturn(new DeleteTableResult());
        DeleteTableResult tableResult = refDataStoreOperator.remove(databaseName, tableName);
        assertThat(tableResult, is(notNullValue()));
    }
}
