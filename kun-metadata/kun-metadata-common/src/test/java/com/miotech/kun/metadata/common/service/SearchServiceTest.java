package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Sets;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.dao.UniversalSearchDao;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.*;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
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

    @Override
    protected boolean usePostgres() {
        return true;
    }

    @Test
    void test_search_simple_one() {
        String keyword = "kun";
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBilder()
                .withDatabaseName("test")
                .withDatasourceAttrs("hive-attr")
                .withDatasourceType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withHighWatermark(DateTimeUtils.now())
                .withDatasourceName("hive-test")
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
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBilder()
                .withDatabaseName("test")
                .withDatasourceAttrs("hive-attr")
                .withDatasourceType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withHighWatermark(DateTimeUtils.now())
                .withDatasourceName("hive-test")
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
        DataSetResourceAttribute beforeRs = DataSetResourceAttribute.Builder.newBilder()
                .withDatabaseName("kun")
                .withDatasourceAttrs("hive-attr")
                .withDatasourceType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withHighWatermark(DateTimeUtils.now())
                .withDatasourceName("hive-test")
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

        keyword = "update";
        DataSetResourceAttribute afterRs = DataSetResourceAttribute.Builder.newBilder()
                .withDatabaseName("kun")
                .withDatasourceAttrs("hive-attr")
                .withDatasourceType("hive")
                .withTags("test")
                .withOwners("update person")
                .withHighWatermark(DateTimeUtils.now())
                .withDatasourceName("hive-prod")
                .build();
        SearchedInfo afterInfo = SearchedInfo.Builder.newBuilder()
                .withGid(globGid)
                .withResourceType(resourceType)
                .withName("test" + "_" + keyword)
                .withDescription("description search")
                .withResourceAttribute(afterRs)
                .withDeleted(false).build();
        searchService.saveOrUpdate(afterInfo);

        getUniversalSearchRequest(new String[]{keyword});
        UniversalSearchInfo afterSearchInfo = searchService.search(request);
        List<SearchedInfo> afterSearchedInfoList = afterSearchInfo.getSearchedInfoList();
        assertThat(afterSearchedInfoList.size(), is(1));
        SearchedInfo afterSearchedInfo = afterSearchedInfoList.get(0);
        assertThat(afterSearchedInfo.getGid(), is(afterSearchedInfo.getGid()));
        DataSetResourceAttribute updateSearchAfter = (DataSetResourceAttribute) afterSearchedInfo.getResourceAttribute();
        assertThat(afterSearchedInfo.getResourceType(), is(afterSearchedInfo.getResourceType()));
        assertThat(afterSearchedInfo.getName(), is(afterSearchedInfo.getName()));
        assertThat(afterSearchedInfo.getDescription(), is(afterSearchedInfo.getDescription()));
        assertThat(updateSearchAfter.getOwners(), is(afterRs.getOwners()));
        assertThat(updateSearchAfter.getDatabaseName(), is(afterRs.getDatabaseName()));
        assertThat(updateSearchAfter.getDatasourceName(), is(afterRs.getDatasourceName()));
        assertThat(updateSearchAfter.getDatasourceAttrs(), is(afterRs.getDatasourceAttrs()));
        assertThat(updateSearchAfter.getTags(), is(afterRs.getTags()));
        assertThat(updateSearchAfter.getHighWatermark(), is(afterRs.getHighWatermark()));

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
        DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder.newBilder()
                .withDatabaseName("kun")
                .withDatasourceAttrs("hive-attr")
                .withDatasourceType("hive")
                .withTags("test,dev,search")
                .withOwners("test person")
                .withHighWatermark(DateTimeUtils.now())
                .withDatasourceName("hive-test")
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