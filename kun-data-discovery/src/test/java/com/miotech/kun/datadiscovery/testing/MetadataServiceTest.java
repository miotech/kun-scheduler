package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasic;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasic;
import com.miotech.kun.datadiscovery.model.entity.UpstreamTask;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.service.MetadataService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockDatasetBasicInfoFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockGlossaryBasicFactory;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicSearch;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskInformation;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

public class MetadataServiceTest extends DiscoveryTestBase {

    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    private MetadataService metadataService;

    @MockBean
    private RestTemplate restTemplate;

    @SpyBean
    private GlossaryService glossaryService;

    @Test
    public void testSearchDatasets() {
        // mock restTemplate
        String searchDatasetsUrl = url + "/dataset/search";
        DatasetBasicInfo datasetBasicInfo = MockDatasetBasicInfoFactory.create();
        DatasetBasicSearch basicSearch = new DatasetBasicSearch(ImmutableList.of(datasetBasicInfo));
        ResponseEntity<DatasetBasicSearch> response = new ResponseEntity<>(basicSearch, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(searchDatasetsUrl), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).willReturn(response);

        // mock glossary service
        GlossaryBasic glossaryBasic = MockGlossaryBasicFactory.create();
        doReturn(ImmutableList.of(glossaryBasic)).when(glossaryService).getGlossariesByDataset(anyLong());

        BasicSearchRequest basicSearchRequest = new BasicSearchRequest();
        DatasetBasicPage datasetBasicPage = metadataService.searchDatasets(basicSearchRequest);

        // verify
        assertThat(datasetBasicPage.getDatasets().size(), is(1));
        DatasetBasic datasetBasic = datasetBasicPage.getDatasets().get(0);
        assertThat(datasetBasic.getGid(), is(datasetBasicInfo.getGid()));
        assertThat(datasetBasic.getName(), is(datasetBasicInfo.getName()));
        assertThat(datasetBasic.getDatasource(), is(datasetBasicInfo.getDatasource()));
        assertThat(datasetBasic.getDatabase(), is(datasetBasicInfo.getDatabase()));
    }

    @Test
    public void testFulltextSearch() {
        // mock restTemplate
        String fullTextSearchUrl = url + "/dataset/full-text/search";
        DatasetBasicInfo datasetBasicInfo = MockDatasetBasicInfoFactory.create();
        DatasetBasicSearch basicSearch = new DatasetBasicSearch(ImmutableList.of(datasetBasicInfo));
        ResponseEntity<DatasetBasicSearch> datasetBasicSearchResponseEntity = new ResponseEntity<>(basicSearch, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullTextSearchUrl), Mockito.eq(HttpMethod.POST), Mockito.any(), Mockito.any(Class.class))).willReturn(datasetBasicSearchResponseEntity);

        String upstreamTaskFetchUrl = url + "/lineage/datasets/upstream-task";
        UpstreamTaskInformation.TaskInformation taskInformation = new UpstreamTaskInformation.TaskInformation(IdGenerator.getInstance().nextId(), "test task", "desc", "scheduled");
        List<UpstreamTaskInformation> upstreamTaskInformationList = ImmutableList.of(new UpstreamTaskInformation(datasetBasicInfo.getGid(), ImmutableList.of(taskInformation)));
        ResponseEntity<List<UpstreamTaskInformation>> upstreamTaskInfoResponseEntity = new ResponseEntity<>(upstreamTaskInformationList, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(upstreamTaskFetchUrl), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.<ParameterizedTypeReference<List<UpstreamTaskInformation>>>any())).willReturn(upstreamTaskInfoResponseEntity);

        // mock glossary service
        GlossaryBasic glossaryBasic = MockGlossaryBasicFactory.create();
        doReturn(ImmutableList.of(glossaryBasic)).when(glossaryService).getGlossariesByDataset(anyLong());

        DatasetBasicPage datasetBasicPage = metadataService.fullTextSearch(new DatasetSearchRequest());

        // verify
        assertThat(datasetBasicPage.getDatasets().size(), is(1));
        DatasetBasic datasetBasic = datasetBasicPage.getDatasets().get(0);
        assertThat(datasetBasic.getGid(), is(datasetBasicInfo.getGid()));
        assertThat(datasetBasic.getName(), is(datasetBasicInfo.getName()));
        assertThat(datasetBasic.getDatasource(), is(datasetBasicInfo.getDatasource()));
        assertThat(datasetBasic.getDatabase(), is(datasetBasicInfo.getDatabase()));
        List<UpstreamTask> upstreamTasks = datasetBasic.getUpstreamTasks();
        assertThat(upstreamTasks.size(), is(1));
        UpstreamTask upstreamTask = upstreamTasks.get(0);
        assertThat(upstreamTask.getId(), is(taskInformation.getId()));
        assertThat(upstreamTask.getName(), is(taskInformation.getName()));
        assertThat(upstreamTask.getDescription(), is(taskInformation.getDescription()));
    }

}
