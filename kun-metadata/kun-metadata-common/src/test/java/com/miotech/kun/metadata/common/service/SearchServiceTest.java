package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Sets;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.dao.UniversalSearchDao;
import com.miotech.kun.metadata.common.factory.MockSearchFactory;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.constant.SearchOperator;
import com.miotech.kun.metadata.core.model.search.*;
import com.miotech.kun.metadata.core.model.vo.ResourceAttributeInfoRequest;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import com.shazam.shazamcrest.MatcherAssert;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SearchServiceTest extends DatabaseTestBase {
    @Inject
    private UniversalSearchDao universalSearchDao;

    @Inject
    private SearchService searchService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    void test_search_Content_limit() {
        SearchedInfo save1 = saveSimple(1L, "test-1", ResourceType.GLOSSARY, "ddd-sss", "zhang-san");
        SearchedInfo save2 = saveSimple(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = saveSimple(3L, "ddd-3", ResourceType.DATASET, "ddd", "test-1");
//        all content
        List<SearchFilterOption> searchFilterOptionList1 = Arrays.stream(new String[]{"test"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request1 = new UniversalSearchRequest();
        request1.setSearchFilterOptions(searchFilterOptionList1);
        UniversalSearchInfo search1 = searchService.search(request1);
        List<SearchedInfo> searchedInfoList1 = search1.getSearchedInfoList();
        assertThat(searchedInfoList1.size(), is(3));
//        one content
        List<SearchFilterOption> searchFilterOptionList2 = Arrays.stream(new String[]{"test"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.NAME))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request2 = new UniversalSearchRequest();
        request2.setSearchFilterOptions(searchFilterOptionList2);
        UniversalSearchInfo search2 = searchService.search(request2);
        List<SearchedInfo> searchedInfoList2 = search2.getSearchedInfoList();
        assertThat(searchedInfoList2.size(), is(1));
        assertThat(searchedInfoList2.get(0).getGid(), is(1L));
//    a part content
        List<SearchFilterOption> searchFilterOptionList3 = Arrays.stream(new String[]{"test"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.ATTRIBUTE, SearchContent.DESCRIPTION))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request3 = new UniversalSearchRequest();
        request3.setSearchFilterOptions(searchFilterOptionList3);
        UniversalSearchInfo search3 = searchService.search(request3);
        List<SearchedInfo> searchedInfoList3 = search3.getSearchedInfoList();
        assertThat(searchedInfoList3.size(), is(2));
        assertThat(searchedInfoList3.get(0).getGid(), is(2L));
        assertThat(searchedInfoList3.get(1).getGid(), is(3L));

    }


    @Test
    void test_search_Search_Filter_Option() {
        SearchedInfo save1 = saveSimple(1L, "test-1", ResourceType.GLOSSARY, "aaa-sss", "zhang-san");
        SearchedInfo save2 = saveSimple(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = saveSimple(3L, "fff-3", ResourceType.DATASET, "ddd", "test-1");

        List<SearchFilterOption> searchFilterOptionList1 = Arrays.stream(new String[]{"test", "ddd"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withSearchOperator(SearchOperator.OR)
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request1 = new UniversalSearchRequest();
        request1.setSearchFilterOptions(searchFilterOptionList1);
        UniversalSearchInfo search1 = searchService.search(request1);
        List<SearchedInfo> searchedInfoList1 = search1.getSearchedInfoList();
        assertThat(searchedInfoList1.size(), is(3));
        assertThat(searchedInfoList1.get(0).getGid(), is(1L));
        assertThat(searchedInfoList1.get(1).getGid(), is(3L));
        assertThat(searchedInfoList1.get(2).getGid(), is(2L));

        List<SearchFilterOption> searchFilterOptionList2 = Arrays.stream(new String[]{"test", "ddd"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withSearchOperator(SearchOperator.OR)
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request2 = new UniversalSearchRequest();
        request2.setSearchFilterOptions(searchFilterOptionList2);
        UniversalSearchInfo search2 = searchService.search(request2);
        List<SearchedInfo> searchedInfoList2 = search2.getSearchedInfoList();
        assertThat(searchedInfoList2.size(), is(3));
    }

    private SearchedInfo saveSimple(Long gid, String name, ResourceType resourceType, String description, String raString) {
        ResourceAttribute resourceAttribute = MockSearchFactory.mockResourceAttribute(raString);
        SearchedInfo searchedInfo = MockSearchFactory.mockSearchedInfo(gid, name, resourceType, description, resourceAttribute, false);
        searchService.saveOrUpdate(searchedInfo);
        return searchedInfo;
    }

    private SearchedInfo save(Long gid, String name, ResourceType resourceType, String description, ResourceAttribute resourceAttribute) {
        SearchedInfo searchedInfo = MockSearchFactory.mockSearchedInfo(gid, name, resourceType, description, resourceAttribute, false);
        searchService.saveOrUpdate(searchedInfo);
        return searchedInfo;
    }

    @Test
    void test_search_simple_one() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive", "kun", "hive-attr", "hive-test", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        searchService.saveOrUpdate(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(datasetSearchedInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(datasetSearchedInfo.getResourceType()));

    }

    @Test
    public void test_search_blank() {
        String keyword = null;
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive", "kun", "hive-attr", "hive-test", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        searchService.saveOrUpdate(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo search = searchService.noneKeywordPage(request);
        assertThat(search.getPageNumber(), is(request.getPageNumber()));
        assertThat(search.getPageSize(), is(request.getPageSize()));
        assertThat(search.getTotalCount(), is(1));
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(datasetSearchedInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(datasetSearchedInfo.getResourceType()));

    }

    @Test
    void test_save_glossary() {
        String keyword = "glossary";
        ResourceAttribute resourceAttribute = MockSearchFactory.mockResourceAttribute("test");
        SearchedInfo glossarySearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.GLOSSARY, "description search test", resourceAttribute, false);

        searchService.saveOrUpdate(glossarySearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(glossarySearchedInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(glossarySearchedInfo.getResourceType()));

    }

    @Test
    void test_save_dataset() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive", "kun", "hive-attr", "hive-test", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        searchService.saveOrUpdate(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(datasetSearchedInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(datasetSearchedInfo.getResourceType()));
    }

    @Test
    void test_update() {
        Long globGid = 1L;
        ResourceType resourceType = ResourceType.DATASET;
        String keyword = "kun";
        DataSetResourceAttribute beforeRs = MockSearchFactory.mockDataSetResourceAttribute("hive-test", "hive-attr", "hive-attr", "hive", "test,dev,search", "test person");
        SearchedInfo beforeInfo = MockSearchFactory.mockSearchedInfo(globGid, "test" + "_" + keyword, resourceType, "description search test", beforeRs, false);

        searchService.saveOrUpdate(beforeInfo);
        String[] strings = {keyword};
        UniversalSearchRequest request = getUniversalSearchRequest(strings);
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(beforeInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(beforeInfo.getResourceType()));
        DataSetResourceAttribute afterRs = MockSearchFactory.mockDataSetResourceAttribute("hive-prod", "hive-attr", "hive-attr", "hive", "test", "update person");
        SearchedInfo afterInfo = MockSearchFactory.mockSearchedInfo(globGid, "test" + "_" + keyword, resourceType, "description search", afterRs, false);
        searchService.saveOrUpdate(afterInfo);

        UniversalSearchInfo afterSearchInfo = searchService.search(request);
        List<SearchedInfo> afterSearchedInfoList = afterSearchInfo.getSearchedInfoList();
        assertThat(afterSearchedInfoList.size(), is(1));
        SearchedInfo afterSearchedInfo = afterSearchedInfoList.get(0);
        assertThat(afterSearchedInfo.getGid(), is(afterSearchedInfo.getGid()));
        DataSetResourceAttribute updateSearchAfter = (DataSetResourceAttribute) afterSearchedInfo.getResourceAttribute();
        assertThat(afterSearchedInfo.getResourceType(), is(afterSearchedInfo.getResourceType()));
        assertThat(afterSearchedInfo.getName(), is(afterSearchedInfo.getName()));
        MatcherAssert.assertThat(updateSearchAfter.getOwners(), Is.is(afterRs.getOwners()));
        MatcherAssert.assertThat(updateSearchAfter.getTags(), Is.is(afterRs.getTags()));
        MatcherAssert.assertThat(updateSearchAfter.getDatabase(), Is.is(afterRs.getDatabase()));
        MatcherAssert.assertThat(updateSearchAfter.getDatasource(), Is.is(afterRs.getDatasource()));
        MatcherAssert.assertThat(updateSearchAfter.getType(), Is.is(afterRs.getType()));
        MatcherAssert.assertThat(updateSearchAfter.getOwners(), Is.is(afterRs.getOwners()));

    }

    @NotNull
    private UniversalSearchRequest getUniversalSearchRequest(String[] strings) {
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(strings)
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setSearchFilterOptions(searchFilterOptionList);
        return request;
    }

    @Test
    void test_remove() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive-test", "kun", "hive-attr", "hive", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        searchService.saveOrUpdate(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        searchService.remove(datasetSearchedInfo);
        UniversalSearchInfo searchRemove = searchService.search(request);
        List<SearchedInfo> searchedInfoListRemove = searchRemove.getSearchedInfoList();
        assertThat(searchedInfoListRemove.size(), is(0));
    }

    @Test
    void test_search_filter_resource_attribute_keyword_blank() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        searchService.saveOrUpdate(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        searchService.saveOrUpdate(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        searchService.saveOrUpdate(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        searchService.saveOrUpdate(datasetSearchedInfo4);
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        UniversalSearchRequest request = MockSearchFactory.mockUniversalSearchRequest(Sets.newHashSet(ResourceType.DATASET), "", Sets.newHashSet(SearchContent.values()), resourceAttributeMap, false);
        UniversalSearchInfo universalSearchInfo = searchService.noneKeywordPage(request);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfo));
        assertThat(universalSearchInfo.getTotalCount(), is(2));
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        boolean match1 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getType).allMatch(s -> s.equals(searchResourceAttribute));
        Assertions.assertTrue(match1);
    }

    @Test
    void test_search_filter_resource_attribute_one() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        searchService.saveOrUpdate(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        searchService.saveOrUpdate(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        searchService.saveOrUpdate(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        searchService.saveOrUpdate(datasetSearchedInfo4);
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        UniversalSearchRequest request = MockSearchFactory.mockUniversalSearchRequest(Sets.newHashSet(ResourceType.DATASET), "name", Sets.newHashSet(SearchContent.values()), resourceAttributeMap, false);
        UniversalSearchInfo universalSearchInfo = searchService.search(request);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfo));
        assertThat(universalSearchInfo.getTotalCount(), is(2));
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        boolean match1 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getType).allMatch(s -> s.equals(searchResourceAttribute));
        Assertions.assertTrue(match1);
        String searchResourceAttribute2 = "datasource_test";
        resourceAttributeMap.put("datasource", searchResourceAttribute2);
    }

    @Test
    void test_search_filter_resource_attribute_more() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        searchService.saveOrUpdate(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        searchService.saveOrUpdate(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        searchService.saveOrUpdate(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        searchService.saveOrUpdate(datasetSearchedInfo4);
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        String searchResourceAttribute2 = "datasource_test";
        resourceAttributeMap.put("datasource", searchResourceAttribute2);
        UniversalSearchRequest request = MockSearchFactory.mockUniversalSearchRequest(Sets.newHashSet(ResourceType.DATASET), "name", Sets.newHashSet(SearchContent.values()), resourceAttributeMap, false);
        UniversalSearchInfo universalSearchInfo = searchService.search(request);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfo));
        assertThat(universalSearchInfo.getTotalCount(), is(1));
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(1));
        boolean match1 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getType).allMatch(s -> s.equals(searchResourceAttribute));
        Assertions.assertTrue(match1);
        boolean match2 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getDatasource).allMatch(s -> s.equals(searchResourceAttribute2));
        Assertions.assertTrue(match2);
    }


    @Test
    void test_search_filter_show_deleted() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        searchService.saveOrUpdate(datasetSearchedInfo1);
        searchService.remove(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        searchService.saveOrUpdate(datasetSearchedInfo2);
        searchService.remove(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        searchService.saveOrUpdate(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        searchService.saveOrUpdate(datasetSearchedInfo4);
        UniversalSearchRequest request = MockSearchFactory.mockUniversalSearchRequest(Sets.newHashSet(ResourceType.DATASET), "name", Sets.newHashSet(SearchContent.values()), null, false);
        UniversalSearchInfo universalSearchInfo = searchService.search(request);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfo));
        assertThat(universalSearchInfo.getTotalCount(), is(2));
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        List<Long> gidList = searchedInfoList.stream().map(SearchedInfo::getGid).collect(Collectors.toList());
        Assertions.assertTrue(gidList.contains(datasetSearchedInfo3.getGid()));
        Assertions.assertTrue(gidList.contains(datasetSearchedInfo4.getGid()));
        UniversalSearchRequest request1 = MockSearchFactory.mockUniversalSearchRequest(Sets.newHashSet(ResourceType.DATASET), "name", Sets.newHashSet(SearchContent.values()), null, true);
        UniversalSearchInfo universalSearchInfo1 = searchService.search(request1);
        Assertions.assertTrue(Objects.nonNull(universalSearchInfo1));
        assertThat(universalSearchInfo1.getTotalCount(), is(4));
        List<SearchedInfo> searchedInfoList1 = universalSearchInfo1.getSearchedInfoList();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList1));
        assertThat(searchedInfoList1.size(), is(4));
        List<Long> gidList1 = searchedInfoList1.stream().map(SearchedInfo::getGid).collect(Collectors.toList());
        Assertions.assertTrue(gidList1.contains(datasetSearchedInfo1.getGid()));
        Assertions.assertTrue(gidList1.contains(datasetSearchedInfo2.getGid()));
        Assertions.assertTrue(gidList1.contains(datasetSearchedInfo3.getGid()));
        Assertions.assertTrue(gidList1.contains(datasetSearchedInfo4.getGid()));

    }


    @Test
    void test_search_resource_attribute_list() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        searchService.saveOrUpdate(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        searchService.saveOrUpdate(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test3", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        searchService.saveOrUpdate(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test4", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        searchService.saveOrUpdate(datasetSearchedInfo4);
        ResourceAttributeInfoRequest request = MockSearchFactory.mockResourceAttributeInfoRequest(ResourceType.DATASET, "type", null, false);
        List<String> types = searchService.fetchResourceAttributeList(request);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(types));
        assertThat(types.size(), is(1));

        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        ResourceAttributeInfoRequest request1 = MockSearchFactory.mockResourceAttributeInfoRequest(ResourceType.DATASET, "datasource", resourceAttributeMap, false);
        List<String> datasourceList = searchService.fetchResourceAttributeList(request1);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(datasourceList));
        assertThat(datasourceList.size(), is(2));

        Map<String, Object> resourceAttributeMap1 = new HashMap<>();
        String searchResourceAttribute1 = "type";
        String searchResourceAttribute2 = "datasource_test2";
        resourceAttributeMap1.put("type", searchResourceAttribute1);
        resourceAttributeMap1.put("datasource", searchResourceAttribute2);
        ResourceAttributeInfoRequest request2 = MockSearchFactory.mockResourceAttributeInfoRequest(ResourceType.DATASET, "datasource", resourceAttributeMap1, false);
        List<String> schemaList = searchService.fetchResourceAttributeList(request2);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(schemaList));
        assertThat(schemaList.size(), is(1));

    }
}