package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasic;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfoWithCount;
import com.miotech.kun.datadiscovery.model.entity.UpstreamTask;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.datadiscovery.service.SearchAppService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockGlossaryBasicFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockSearchInfoFactory;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskInformation;
import com.miotech.kun.workflow.core.model.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class MetadataServiceTest extends DataDiscoveryTestBase {

    @Value("${metadata.base-url:localhost:8084}")
    private String url;

    @Autowired
    private MetadataService metadataService;

    @MockBean
    private SearchAppService searchAppService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private WorkflowClient workflowClient;

    @SpyBean
    private GlossaryService glossaryService;

    @MockBean
    private DeployedTaskFacade deployedTaskFacade;
    @MockBean
    private LineageAppService lineageAppService;


    @Test
    public void testSearchDatasets() {
        // mock restTemplate
        String keyword = "test";
        BasicSearchRequest basicSearchRequest = new DatasetSearchRequest();
        basicSearchRequest.setKeyword(keyword);
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource("hive")
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo searchedInfo = MockSearchInfoFactory.mockDataSetSearch(keyword, resourceAttribute);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo(ImmutableList.of(searchedInfo));
        when(searchAppService.searchDataSet(basicSearchRequest)).thenReturn(universalSearchInfo);
        DatasetBasicPage datasetBasicPage = metadataService.searchDatasets(basicSearchRequest);
        // verify
        assertThat(datasetBasicPage.getDatasets().size(), is(1));
        DatasetBasic datasetBasic = datasetBasicPage.getDatasets().get(0);
        assertThat(datasetBasic.getGid(), is(searchedInfo.getGid()));
        assertThat(datasetBasic.getName(), is(searchedInfo.getName()));
        assertThat(datasetBasic.getDatasource(), is(resourceAttribute.getDatasource()));
        assertThat(datasetBasic.getDatabase(), is(resourceAttribute.getDatabase()));
    }

    @Test
    public void testFulltextSearch() {
        // mock restTemplate
        String keyword = "test";
        DatasetSearchRequest datasetSearchRequest = new DatasetSearchRequest();
        datasetSearchRequest.setKeyword(keyword);
        String datasource = "test";
        datasetSearchRequest.setDatasource(datasource);
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource(datasource)
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo searchedInfo = MockSearchInfoFactory.mockDataSetSearch(keyword, resourceAttribute);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo(ImmutableList.of(searchedInfo));
        when(searchAppService.searchFullDataSet(datasetSearchRequest)).thenReturn(universalSearchInfo);


        Task task = Task.newBuilder().withId(IdGenerator.getInstance().nextId()).withName("test task").withDescription("desc").withDependencies(ImmutableList.of()).withTags(ImmutableList.of(new Tag("type", "scheduled"))).build();
        List<UpstreamTaskInformation> upstreamTaskInformationList = ImmutableList.of(new UpstreamTaskInformation(searchedInfo.getGid(), ImmutableList.of(task)));
        when(lineageAppService.getUpstreamTaskInformation(anyList())).thenReturn(upstreamTaskInformationList);

        // mock deployedTaskFacade
        DeployedTask deployedTask = DeployedTask.newBuilder().withDefinitionId(IdGenerator.getInstance().nextId()).build();
        Map<Long, DeployedTask> deployedTaskMap = ImmutableMap.of(task.getId(), deployedTask);
        doReturn(deployedTaskMap).when(deployedTaskFacade).findByWorkflowTaskIds(ImmutableList.of(task.getId()));

        // mock glossary service
        GlossaryBasicInfoWithCount glossaryBasic = MockGlossaryBasicFactory.createGlossaryBasicInfoWithCount();
        doReturn(ImmutableList.of(glossaryBasic)).when(glossaryService).getGlossariesByDataset(anyLong());

        DatasetBasicPage datasetBasicPage = metadataService.fullTextSearch(datasetSearchRequest);

        // verify
        assertThat(datasetBasicPage.getDatasets().size(), is(1));
        DatasetBasic datasetBasic = datasetBasicPage.getDatasets().get(0);
        assertThat(datasetBasic.getGid(), is(searchedInfo.getGid()));
        assertThat(datasetBasic.getName(), is(searchedInfo.getName()));
        assertThat(datasetBasic.getDatasource(), is(resourceAttribute.getDatasource()));
        assertThat(datasetBasic.getDatabase(), is(resourceAttribute.getDatabase()));
        List<UpstreamTask> upstreamTasks = datasetBasic.getUpstreamTasks();
        assertThat(upstreamTasks.size(), is(1));
        UpstreamTask upstreamTask = upstreamTasks.get(0);
        assertThat(upstreamTask.getId(), is(task.getId()));
        assertThat(upstreamTask.getName(), is(task.getName()));
        assertThat(upstreamTask.getDescription(), is(task.getDescription()));
        assertThat(upstreamTask.getDefinitionId(), is(deployedTask.getDefinitionId()));
    }

}
