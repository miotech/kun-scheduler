package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Sets;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.dao.UniversalSearchDao;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.constant.SearchOperator;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import com.shazam.shazamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
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
        SearchedInfo save1 = save(1L, "test-1", ResourceType.GLOSSARY, "ddd-sss", "zhang-san");
        SearchedInfo save2 = save(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = save(3L, "ddd-3", ResourceType.DATASET, "ddd", "test-1");
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
        SearchedInfo save1 = save(1L, "test-1", ResourceType.GLOSSARY, "aaa-sss", "zhang-san");
        SearchedInfo save2 = save(2L, "bbb-2", ResourceType.GLOSSARY, "test-1", "zhang-san");
        SearchedInfo save3 = save(3L, "fff-3", ResourceType.DATASET, "ddd", "test-1");

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

    private SearchedInfo save(Long gid, String name, ResourceType resourceType, String description, String raString) {
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withOwners(raString)
                .build();
        SearchedInfo searchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(gid)
                .withResourceType(resourceType)
                .withName(name)
                .withDescription(description)
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false)
                .build();
        searchService.saveOrUpdate(searchedInfo);
        return searchedInfo;
    }

    @Test
    void test_search_simple_one() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("test")
                .withDatasource("hive-attr")
                .withType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withSchema("hive-test")
                .build();
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(1L)
                .withResourceType(ResourceType.GLOSSARY)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
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
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("test")
                .withDatasource("hive-attr")
                .withType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withSchema("hive-test")
                .build();
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(1L)
                .withResourceType(ResourceType.GLOSSARY)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
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
        SearchedInfo glossarySearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(1L)
                .withResourceType(ResourceType.GLOSSARY)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(new GlossaryResourceAttribute("test"))
                .withDeleted(false).build();
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
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("test")
                .withDatasource("hive-attr")
                .withType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withSchema("hive-test")
                .build();
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(1L)
                .withResourceType(ResourceType.DATASET)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
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
        DataSetResourceAttribute beforeRs = DataSetResourceAttribute.Builder.newBuilder()
                .withDatabase("kun")
                .withDatasource("hive-attr")
                .withSchema("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withType("hive-test")
                .build();
        SearchedInfo beforeInfo = SearchedInfo.Builder.newBuilder()
                .withGid(globGid)
                .withResourceType(resourceType)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(beforeRs)
                .withDeleted(false).build();
        searchService.saveOrUpdate(beforeInfo);
        String[] strings = {keyword};
        UniversalSearchRequest request = getUniversalSearchRequest(strings);
        UniversalSearchInfo search = searchService.search(request);
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        SearchedInfo searchedInfo = searchedInfoList.get(0);
        assertThat(searchedInfo.getGid(), is(beforeInfo.getGid()));
        assertThat(searchedInfo.getResourceType(), is(beforeInfo.getResourceType()));

        DataSetResourceAttribute afterRs = DataSetResourceAttribute.Builder.newBuilder()
                .withDatasource("kun")
                .withDatabase("hive-attr")
                .withSchema("hive")
                .withTags("test")
                .withOwners("update person")
                .withType("hive-prod")
                .build();
        SearchedInfo afterInfo = SearchedInfo.Builder.newBuilder()
                .withGid(globGid)
                .withResourceType(resourceType)
                .withName("test" + "_" + keyword)
                .withDescription("description search")
                .withResourceAttribute(afterRs)
                .withDeleted(false).build();
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
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBuilder()
                .withDatasource("kun")
                .withDatabase("hive-attr")
                .withSchema("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withType("hive-test")
                .build();
        SearchedInfo datasetSearchedInfo = SearchedInfo.Builder.newBuilder()
                .withGid(1L)
                .withResourceType(ResourceType.GLOSSARY)
                .withName("test" + "_" + keyword)
                .withDescription("description search test")
                .withResourceAttribute(resourceAttribute)
                .withDeleted(false).build();
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
}