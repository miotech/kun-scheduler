package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.ResourceAttributeRequest;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.datadiscovery.service.SearchAppService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockGlossaryBasicFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockSearchInfoFactory;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-05-16 09:53
 **/
public class SearchAppServiceTest extends DataDiscoveryTestBase {
    @Value("${metadata.base-url:localhost:8084}")
    private String url;
    @Autowired
    private SearchAppService searchAppService;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private DeployedTaskFacade deployedTaskFacade;
    @MockBean
    private WorkflowClient workflowClient;

    @Test
    public void test_full_search() {
        String keyword = "test";
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource("datasource")
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo searchedInfo = MockSearchInfoFactory.mockDataSetSearch(keyword, resourceAttribute);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo(ImmutableList.of(searchedInfo));
        BDDMockito.given(restTemplate.getForEntity(anyString(), Mockito.eq(UniversalSearchInfo.class)))
                .willReturn(new ResponseEntity<>(universalSearchInfo, HttpStatus.OK));
        UniversalSearchInfo universalSearchInfoResult = searchAppService.fullSearch(keyword);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfoResult));
        Assertions.assertTrue(CollectionUtils.isNotEmpty(universalSearchInfoResult.getSearchedInfoList()));

    }

    @Test
    public void test_search_glossary() {
        String keyword = "test";
        UniversalSearchInfo universalSearchInfo = MockSearchInfoFactory.mockSearchGlossary(ImmutableList.of(MockGlossaryBasicFactory.createGlossary(keyword)), keyword);
        BasicSearchRequest basicSearchRequest = MockSearchInfoFactory.mockBasicSearchRequest(keyword);
        String fullUrl = url + "/search/options";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUrl), Mockito.eq(HttpMethod.POST), any(),
                        Mockito.<ParameterizedTypeReference<UniversalSearchInfo>>any()))
                .willReturn(new ResponseEntity<>(universalSearchInfo, HttpStatus.OK));
        UniversalSearchInfo universalSearchInfoResult = searchAppService.searchGlossary(basicSearchRequest);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfoResult));
        List<SearchedInfo> searchedInfoList = universalSearchInfoResult.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.get(0).getResourceType(), is(ResourceType.GLOSSARY));
    }

    @Test
    public void test_search_dataset() {
        String keyword = "test";
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource("datasource")
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo searchedInfo = MockSearchInfoFactory.mockDataSetSearch(keyword, resourceAttribute);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo(ImmutableList.of(searchedInfo));
        BasicSearchRequest basicSearchRequest = MockSearchInfoFactory.mockBasicSearchRequest(keyword);
        String fullUrl = url + "/search/options";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUrl), Mockito.eq(HttpMethod.POST), any(),
                        Mockito.<ParameterizedTypeReference<UniversalSearchInfo>>any()))
                .willReturn(new ResponseEntity<>(universalSearchInfo, HttpStatus.OK));
        UniversalSearchInfo universalSearchInfoResult = searchAppService.searchDataSet(basicSearchRequest);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfoResult));
        List<SearchedInfo> searchedInfoList = universalSearchInfoResult.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.get(0).getResourceType(), is(ResourceType.DATASET));

    }

    @Test
    public void test_search_full_dataset() {
        String keyword = "test";
        String datasource = "datasource";
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("default")
                .withDatasource("datasource")
                .withType("hive")
                .withTags("tag1")
                .withOwners("admin")
                .withSchema("public")
                .build();
        SearchedInfo searchedInfo = MockSearchInfoFactory.mockDataSetSearch(keyword, resourceAttribute);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo(ImmutableList.of(searchedInfo));
        String fullUrl = url + "/search/options";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUrl), Mockito.eq(HttpMethod.POST), any(),
                        Mockito.<ParameterizedTypeReference<UniversalSearchInfo>>any()))
                .willReturn(new ResponseEntity<>(universalSearchInfo, HttpStatus.OK));
        DatasetSearchRequest datasetSearchRequest = new DatasetSearchRequest();
        datasetSearchRequest.setKeyword(keyword);
        datasetSearchRequest.setDatasource(datasource);
        UniversalSearchInfo universalSearchInfoResult = searchAppService.searchFullDataSet(datasetSearchRequest);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfoResult));
        List<SearchedInfo> searchedInfoList = universalSearchInfoResult.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.get(0).getResourceType(), is(ResourceType.DATASET));

    }

    @Test
    public void test_fetch_resource_attribute_list() {
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String resourceAttributeName = "datasource";
        resourceAttributeMap.put("type", "type");
        ResourceAttributeRequest resourceAttributeRequest = MockSearchInfoFactory.mockDResourceAttributeRequest(resourceAttributeName, resourceAttributeMap);
        ImmutableList<String> mockList = ImmutableList.of("test1", "test2");
        String fullUrl = url + "/search/attribute/list";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUrl), Mockito.eq(HttpMethod.POST), any(),
                        Mockito.<ParameterizedTypeReference<List<String>>>any()))
                .willReturn(new ResponseEntity<>(mockList, HttpStatus.OK));
        List<String> list = searchAppService.fetchResourceAttributeList(ResourceType.DATASET, resourceAttributeRequest);
        assertThat(list, is(mockList));
    }

    @Test
    public void test_save_or_update_glossary_search_info() {
        GlossaryBasicInfo glossaryBasicInfo = MockGlossaryBasicFactory.createGlossaryBasicInfo("test");
        String suggestColumnUrl = url + "/search/update";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(suggestColumnUrl), Mockito.eq(HttpMethod.POST), any(), (ParameterizedTypeReference<Void>) any()))
                .willReturn(new ResponseEntity<>(HttpStatus.OK));

        searchAppService.saveOrUpdateGlossarySearchInfo(glossaryBasicInfo);
        Assertions.assertTrue(true);
    }

    @Test
    public void test_remove_glossary_search_info() {
        GlossaryBasicInfo glossaryBasicInfo = MockGlossaryBasicFactory.createGlossaryBasicInfo("test");
        String suggestColumnUrl = url + "/search/remove";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(suggestColumnUrl), Mockito.eq(HttpMethod.POST), any(), (ParameterizedTypeReference<Void>) any()))
                .willReturn(new ResponseEntity<>(HttpStatus.OK));
        searchAppService.removeGlossarySearchInfo(glossaryBasicInfo.getId());
        Assertions.assertTrue(true);
    }
}
