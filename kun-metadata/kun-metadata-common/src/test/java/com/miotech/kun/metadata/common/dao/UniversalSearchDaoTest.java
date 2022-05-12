package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockSearchFactory;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.constant.SearchOperator;
import com.miotech.kun.metadata.core.model.search.*;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import com.shazam.shazamcrest.MatcherAssert;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UniversalSearchDaoTest extends DatabaseTestBase {

    @Inject
    private UniversalSearchDao universalSearchDao;

    @Test
    void test_search_Content_limit() {
        SearchedInfo save1 = saveSimple(1L, "test-1", ResourceType.GLOSSARY, "ddd-sss", "zhang-san");
        SearchedInfo save2 = saveSimple(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = saveSimple(3L, "ddd-3", ResourceType.DATASET, "ddd", "test-1");
//        all content
        List<SearchFilterOption> searchFilterOptionList1 = Arrays.stream(new String[]{"test"}).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request1 = new UniversalSearchRequest();
        request1.setSearchFilterOptions(searchFilterOptionList1);
        List<SearchedInfo> searchedInfoList1 = universalSearchDao.search(searchFilterOptionList1, request1.getResourceTypeNames(), request1.getResourceAttributeMap(), request1.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList1.size(), Is.is(3));
//        one content
        List<SearchFilterOption> searchFilterOptionList2 = Arrays.stream(new String[]{"test"}).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.NAME)).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request2 = new UniversalSearchRequest();
        request2.setSearchFilterOptions(searchFilterOptionList2);
        List<SearchedInfo> searchedInfoList2 = universalSearchDao.search(searchFilterOptionList2, request2.getResourceTypeNames(), request2.getResourceAttributeMap(), request2.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList2.size(), Is.is(1));
        MatcherAssert.assertThat(searchedInfoList2.get(0).getGid(), Is.is(1L));
//    a part content
        List<SearchFilterOption> searchFilterOptionList3 = Arrays.stream(new String[]{"test"}).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.ATTRIBUTE, SearchContent.DESCRIPTION)).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request3 = new UniversalSearchRequest();
        request3.setSearchFilterOptions(searchFilterOptionList3);
        List<SearchedInfo> searchedInfoList3 = universalSearchDao.search(searchFilterOptionList3, request2.getResourceTypeNames(), request2.getResourceAttributeMap(), request2.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList3.size(), Is.is(2));
        MatcherAssert.assertThat(searchedInfoList3.get(0).getGid(), Is.is(2L));
        MatcherAssert.assertThat(searchedInfoList3.get(1).getGid(), Is.is(3L));

    }


    @Test
    void test_search_Search_Filter_Option() {
        SearchedInfo save1 = saveSimple(1L, "test-1", ResourceType.GLOSSARY, "aaa-sss", "zhang-san");
        SearchedInfo save2 = saveSimple(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = saveSimple(3L, "fff-3", ResourceType.DATASET, "ddd", "test-1");

        List<SearchFilterOption> searchFilterOptionList1 = Arrays.stream(new String[]{"test", "ddd"}).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withSearchOperator(SearchOperator.OR).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request1 = new UniversalSearchRequest();
        request1.setSearchFilterOptions(searchFilterOptionList1);
        List<SearchedInfo> searchedInfoList1 = universalSearchDao.search(searchFilterOptionList1, request1.getResourceTypeNames(), request1.getResourceAttributeMap(), request1.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList1.size(), Is.is(3));
        MatcherAssert.assertThat(searchedInfoList1.get(0).getGid(), Is.is(1L));
        MatcherAssert.assertThat(searchedInfoList1.get(1).getGid(), Is.is(3L));
        MatcherAssert.assertThat(searchedInfoList1.get(2).getGid(), Is.is(2L));

        List<SearchFilterOption> searchFilterOptionList2 = Arrays.stream(new String[]{"test", "ddd"}).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withSearchOperator(SearchOperator.OR).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request2 = new UniversalSearchRequest();
        request2.setSearchFilterOptions(searchFilterOptionList2);
        List<SearchedInfo> searchedInfoList2 = universalSearchDao.search(searchFilterOptionList2, request2.getResourceTypeNames(), request2.getResourceAttributeMap(), request2.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList2.size(), Is.is(3));


    }

    private SearchedInfo saveSimple(Long gid, String name, ResourceType resourceType, String description, String raString) {
        ResourceAttribute resourceAttribute = MockSearchFactory.mockResourceAttribute(raString);
        SearchedInfo searchedInfo = MockSearchFactory.mockSearchedInfo(gid, name, resourceType, description, resourceAttribute, false);
        universalSearchDao.save(searchedInfo);
        return searchedInfo;
    }

    private SearchedInfo save(Long gid, String name, ResourceType resourceType, String description, ResourceAttribute resourceAttribute) {
        SearchedInfo searchedInfo = MockSearchFactory.mockSearchedInfo(gid, name, resourceType, description, resourceAttribute, false);
        universalSearchDao.save(searchedInfo);
        return searchedInfo;
    }


    @Test
    void test_save_glossary() {
        String keyword = "glossary";
        ResourceAttribute resourceAttribute = MockSearchFactory.mockResourceAttribute("test");
        SearchedInfo glossarySearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.GLOSSARY, "description search test", resourceAttribute, false);
        universalSearchDao.save(glossarySearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList.size(), Is.is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        MatcherAssert.assertThat(searchedInfo.getGid(), Is.is(glossarySearchedInfo.getGid()));
        MatcherAssert.assertThat(searchedInfo.getResourceType(), Is.is(glossarySearchedInfo.getResourceType()));

    }

    @Test
    void test_save_dataset() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive", "kun", "hive-attr", "hive-test", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        universalSearchDao.save(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList.size(), Is.is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        MatcherAssert.assertThat(searchedInfo.getGid(), Is.is(datasetSearchedInfo.getGid()));
        MatcherAssert.assertThat(searchedInfo.getResourceType(), Is.is(datasetSearchedInfo.getResourceType()));
    }

    @Test
    void test_update() {
        Long globGid = 1L;
        ResourceType resourceType = ResourceType.DATASET;
        String keyword = "kun";
        DataSetResourceAttribute beforeRs = MockSearchFactory.mockDataSetResourceAttribute("hive-test", "hive-attr", "hive-attr", "hive", "test,dev,search", "test person");
        SearchedInfo beforeInfo = MockSearchFactory.mockSearchedInfo(globGid, "test" + "_" + keyword, resourceType, "description search test", beforeRs, false);
        universalSearchDao.save(beforeInfo);
        String[] strings = {keyword};
        UniversalSearchRequest request = getUniversalSearchRequest(strings);
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList.size(), Is.is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        MatcherAssert.assertThat(searchedInfo.getGid(), Is.is(beforeInfo.getGid()));
        MatcherAssert.assertThat(searchedInfo.getResourceType(), Is.is(beforeInfo.getResourceType()));

        keyword = "update";
        String[] strings1 = {keyword};
        UniversalSearchRequest request1 = getUniversalSearchRequest(strings1);
        DataSetResourceAttribute afterRs = MockSearchFactory.mockDataSetResourceAttribute("hive-prod", "hive-attr", "hive-attr", "hive", "test", "update person");
        SearchedInfo afterInfo = MockSearchFactory.mockSearchedInfo(globGid, "test" + "_" + keyword, resourceType, "description search", afterRs, false);
        universalSearchDao.update(afterInfo);

        List<SearchedInfo> afterSearchedInfoList = universalSearchDao.search(request1.getSearchFilterOptions(), request1.getResourceTypeNames(), request1.getResourceAttributeMap(), request1.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(afterSearchedInfoList.size(), Is.is(1));
        SearchedInfo afterSearchedInfo = afterSearchedInfoList.get(0);
        MatcherAssert.assertThat(afterSearchedInfo.getGid(), Is.is(afterSearchedInfo.getGid()));
        DataSetResourceAttribute updateSearchAfter = (DataSetResourceAttribute) afterSearchedInfo.getResourceAttribute();
        MatcherAssert.assertThat(afterSearchedInfo.getResourceType(), Is.is(afterSearchedInfo.getResourceType()));
        MatcherAssert.assertThat(afterSearchedInfo.getName(), Is.is(afterSearchedInfo.getName()));
        MatcherAssert.assertThat(afterSearchedInfo.getDescription(), Is.is(afterSearchedInfo.getDescription()));
        MatcherAssert.assertThat(updateSearchAfter.getOwners(), Is.is(afterRs.getOwners()));
        MatcherAssert.assertThat(updateSearchAfter.getTags(), Is.is(afterRs.getTags()));
        MatcherAssert.assertThat(updateSearchAfter.getDatabase(), Is.is(afterRs.getDatabase()));
        MatcherAssert.assertThat(updateSearchAfter.getDatasource(), Is.is(afterRs.getDatasource()));
        MatcherAssert.assertThat(updateSearchAfter.getType(), Is.is(afterRs.getType()));
        MatcherAssert.assertThat(updateSearchAfter.getOwners(), Is.is(afterRs.getOwners()));

    }

    @NotNull
    private UniversalSearchRequest getUniversalSearchRequest(String[] strings) {
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(strings).map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withKeyword(s).build()).collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setSearchFilterOptions(searchFilterOptionList);
        return request;
    }

    @Test
    void test_remove() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive-test", "kun", "hive-attr", "hive", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        universalSearchDao.save(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoList.size(), Is.is(1));
        universalSearchDao.remove(datasetSearchedInfo.getResourceType(), datasetSearchedInfo.getGid());
        List<SearchedInfo> searchedInfoListRemove = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        MatcherAssert.assertThat(searchedInfoListRemove.size(), Is.is(0));
    }

    @Test
    void test_search_filter_symbol() {
        String keyword = "'''！！test特殊符号@¥#%%#…………#……";
        DataSetResourceAttribute resourceAttribute = MockSearchFactory.mockDataSetResourceAttribute("hive-test", "kun", "hive-attr", "hive", "test,dev,search", "test person");
        SearchedInfo datasetSearchedInfo = MockSearchFactory.mockSearchedInfo(1L, "test" + "_" + keyword, ResourceType.DATASET, "description search test", resourceAttribute, false);
        universalSearchDao.save(datasetSearchedInfo);
        UniversalSearchRequest request = getUniversalSearchRequest(new String[]{keyword});
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(request.getSearchFilterOptions(), request.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(datasetSearchedInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(datasetSearchedInfo.getResourceType()));

        UniversalSearchRequest request2 = getUniversalSearchRequest(new String[]{datasetSearchedInfo.getName()});
        List<SearchedInfo> searchedInfoList2 = universalSearchDao.search(request2.getSearchFilterOptions(), request2.getResourceTypeNames(), request.getResourceAttributeMap(), request.isShowDeleted(), 1, 10);
        assertThat(searchedInfoList2.size(), is(1));
        SearchedInfo searchedInfo2 = searchedInfoList2.get(0);
        assertThat(searchedInfo2.getGid(), is(datasetSearchedInfo.getGid()));
        assertThat(searchedInfo2.getResourceType(), is(datasetSearchedInfo.getResourceType()));
    }

    @Test
    void test_search_filter_resource_attribute_keyword_blank() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        universalSearchDao.save(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        universalSearchDao.save(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        universalSearchDao.save(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        universalSearchDao.save(datasetSearchedInfo4);
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        List<SearchedInfo> searchedInfoList = universalSearchDao.noneKeywordPage(Sets.newHashSet(ResourceType.DATASET.name()), resourceAttributeMap, false, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        boolean match1 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getType).allMatch(s -> s.equals(searchResourceAttribute));
        Assertions.assertTrue(match1);
        String searchResourceAttribute2 = "datasource_test";
        resourceAttributeMap.put("datasource", searchResourceAttribute2);
    }

    @Test
    void test_search_filter_resource_attribute_one() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        universalSearchDao.save(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        universalSearchDao.save(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        universalSearchDao.save(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        universalSearchDao.save(datasetSearchedInfo4);
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{"name"})
                .map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withKeyword(s).build())
                .collect(Collectors.toList());
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(searchFilterOptionList, Sets.newHashSet(ResourceType.DATASET.name()), resourceAttributeMap, false, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        boolean match1 = searchedInfoList.stream().map(searchedInfo -> (DataSetResourceAttribute) searchedInfo.getResourceAttribute()).map(DataSetResourceAttribute::getType).allMatch(s -> s.equals(searchResourceAttribute));
        Assertions.assertTrue(match1);
    }

    @Test
    void test_search_filter_resource_attribute_more() {
        DataSetResourceAttribute resourceAttribute1 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo1 = MockSearchFactory.mockSearchedInfo(1L, "name", ResourceType.DATASET, "description", resourceAttribute1, false);
        universalSearchDao.save(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        universalSearchDao.save(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        universalSearchDao.save(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        universalSearchDao.save(datasetSearchedInfo4);
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{"name"})
                .map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withKeyword(s).build())
                .collect(Collectors.toList());
        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        String searchResourceAttribute2 = "datasource_test";
        resourceAttributeMap.put("datasource", searchResourceAttribute2);
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(searchFilterOptionList, Sets.newHashSet(ResourceType.DATASET.name()), resourceAttributeMap, false, 1, 10);
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
        universalSearchDao.save(datasetSearchedInfo1);
        universalSearchDao.remove(datasetSearchedInfo1.getResourceType(), datasetSearchedInfo1.getGid());
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        universalSearchDao.save(datasetSearchedInfo2);
        universalSearchDao.remove(datasetSearchedInfo2.getResourceType(), datasetSearchedInfo2.getGid());
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        universalSearchDao.save(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        universalSearchDao.save(datasetSearchedInfo4);
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{"name"})
                .map(s -> SearchFilterOption.Builder.newBuilder().withSearchContents(Sets.newHashSet(SearchContent.values())).withKeyword(s).build())
                .collect(Collectors.toList());
        List<SearchedInfo> searchedInfoList = universalSearchDao.search(searchFilterOptionList, Sets.newHashSet(ResourceType.DATASET.name()), null, false, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(searchedInfoList));
        assertThat(searchedInfoList.size(), is(2));
        List<Long> gidList = searchedInfoList.stream().map(SearchedInfo::getGid).collect(Collectors.toList());
        Assertions.assertTrue(gidList.contains(datasetSearchedInfo3.getGid()));
        Assertions.assertTrue(gidList.contains(datasetSearchedInfo4.getGid()));

        List<SearchedInfo> searchedInfoList1 = universalSearchDao.search(searchFilterOptionList, Sets.newHashSet(ResourceType.DATASET.name()), null, true, 1, 10);
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
        universalSearchDao.save(datasetSearchedInfo1);
        DataSetResourceAttribute resourceAttribute2 = MockSearchFactory.mockDataSetResourceAttribute("type", "datasource_test2", "database_test2", "schema_test2", "tag_test2", "owner_test2");
        SearchedInfo datasetSearchedInfo2 = MockSearchFactory.mockSearchedInfo(2L, "name", ResourceType.DATASET, "description", resourceAttribute2, false);
        universalSearchDao.save(datasetSearchedInfo2);
        DataSetResourceAttribute resourceAttribute3 = MockSearchFactory.mockDataSetResourceAttribute("", "datasource_test3", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo3 = MockSearchFactory.mockSearchedInfo(3L, "name", ResourceType.DATASET, "description", resourceAttribute3, false);
        universalSearchDao.save(datasetSearchedInfo3);
        DataSetResourceAttribute resourceAttribute4 = MockSearchFactory.mockDataSetResourceAttribute(null, "datasource_test4", "database_test", "schema_test", "tag_test", "owner_test");
        SearchedInfo datasetSearchedInfo4 = MockSearchFactory.mockSearchedInfo(4L, "name", ResourceType.DATASET, "description", resourceAttribute4, false);
        universalSearchDao.save(datasetSearchedInfo4);
        List<String> types = universalSearchDao.fetchResourceAttributeList(ResourceType.DATASET, "type", null, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(types));
        assertThat(types.size(), is(1));

        Map<String, Object> resourceAttributeMap = new HashMap<>();
        String searchResourceAttribute = "type";
        resourceAttributeMap.put("type", searchResourceAttribute);
        List<String> datasourceList = universalSearchDao.fetchResourceAttributeList(ResourceType.DATASET, "datasource", resourceAttributeMap, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(datasourceList));
        assertThat(datasourceList.size(), is(2));

        Map<String, Object> resourceAttributeMap1 = new HashMap<>();
        String searchResourceAttribute1 = "type";
        String searchResourceAttribute2 = "datasource_test2";
        resourceAttributeMap1.put("type", searchResourceAttribute1);
        resourceAttributeMap1.put("datasource", searchResourceAttribute2);
        List<String> schemaList = universalSearchDao.fetchResourceAttributeList(ResourceType.DATASET, "schema", resourceAttributeMap1, false);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(schemaList));
        assertThat(schemaList.size(), is(1));

    }
}
