package com.miotech.kun.metadata.common.backend;

import com.miotech.kun.metadata.common.cataloger.CatalogerBlackList;
import com.miotech.kun.metadata.common.cataloger.CatalogerConfig;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.mock.MockMetadataBackend;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.GlueConnectionInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class BaseMetadataBackendTest {


    @Test
    public void databaseInBlackList_should_be_filtered_out(){
        //prepare
        CatalogerBlackList blackList = new CatalogerBlackList();
        blackList.addMember("database2");
        CatalogerConfig config = new CatalogerConfig(null,blackList);
        MockMetadataBackend metadataBackend = new MockMetadataBackend(config);
        GlueConnectionInfo connectionInfo = new GlueConnectionInfo(ConnectionType.GLUE, "asskey", "secretkey", "region");
        DataSource hive = MockDataSourceFactory.createDataSource(1, "hive", connectionInfo, DatasourceType.HIVE, new ArrayList<>());
        //prepare data
        List<String> databaseList = Arrays.asList("database1","database2","database3");
        metadataBackend.setDatabasesForDatasource(hive.getId(),databaseList);
        Dataset datasetOfDatabase1 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"database1");
        Dataset datasetOfDatabase2 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"database2");
        Dataset datasetOfDatabase3 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"database3");
        metadataBackend.setDataSetsForDatabase("database1",Arrays.asList(datasetOfDatabase1));
        metadataBackend.setDataSetsForDatabase("database2",Arrays.asList(datasetOfDatabase2));
        metadataBackend.setDataSetsForDatabase("database3",Arrays.asList(datasetOfDatabase3));

        Iterator<Dataset> datasetIterator = metadataBackend.extract(hive);

        //verify
        List<Dataset> extractedDataset = new ArrayList<>();
        datasetIterator.forEachRemaining(extractedDataset::add);
        assertThat(extractedDataset,hasSize(2));
        assertThat(extractedDataset,containsInAnyOrder(datasetOfDatabase1,datasetOfDatabase3));
    }

    @Test
    public void tableInBlackList_should_be_filtered_out(){
        //prepare
        CatalogerBlackList blackList = new CatalogerBlackList();
        blackList.addMember("database1:table1");
        CatalogerConfig config = new CatalogerConfig(null,blackList);
        MockMetadataBackend metadataBackend = new MockMetadataBackend(config);
        GlueConnectionInfo connectionInfo = new GlueConnectionInfo(ConnectionType.GLUE, "asskey", "secretkey", "region");
        DataSource hive = MockDataSourceFactory.createDataSource(1, "hive", connectionInfo, DatasourceType.HIVE, new ArrayList<>());
        //prepare data
        List<String> databaseList = Arrays.asList("database1","database2","database3");
        metadataBackend.setDatabasesForDatasource(hive.getId(),databaseList);
        Dataset dataset1OfDatabase1 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"table1","database1");
        Dataset dataset2OfDatabase1 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"table2","database1");
        Dataset dataset1OfDatabase2 = MockDatasetFactory.createDatasetWithDatabase(hive.getId(),"table1","database2");
        metadataBackend.setDataSetsForDatabase("database1",Arrays.asList(dataset1OfDatabase1,dataset2OfDatabase1));
        metadataBackend.setDataSetsForDatabase("database2",Arrays.asList(dataset1OfDatabase2));
        metadataBackend.setDataSetsForDatabase("database3",Arrays.asList());


        Iterator<Dataset> datasetIterator = metadataBackend.extract(hive);

        //verify
        List<Dataset> extractedDataset = new ArrayList<>();
        datasetIterator.forEachRemaining(extractedDataset::add);
        assertThat(extractedDataset,hasSize(2));
        assertThat(extractedDataset,containsInAnyOrder(dataset2OfDatabase1,dataset1OfDatabase2));

    }
}
