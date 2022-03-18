package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.SearchService;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: search controller
 * @author: zemin  huang
 * @create: 2022-03-08 10:01
 **/
@Singleton
public class SearchController {

    private static Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Inject
    private SearchService searchService;

    /**
     * @param keywords and
     * @return UniversalSearchInfo
     */
    @RouteMapping(url = "/search/full", method = "GET")
    public UniversalSearchInfo fullSearch(@RequestBody String... keywords) {
        logger.debug("SearchController fullTextSearch  keyword: {}", keywords);
        Preconditions.checkNotNull(keywords, " Invalid parameter `keywords`: found null object");
        Preconditions.checkArgument(keywords.length>0, " Invalid parameter `keywords`: found empty object");
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(keywords)
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setSearchFilterOptions(searchFilterOptionList);
        return searchService.search(request);
    }


    /**
     * search
     *
     * @param request UniversalSearchRequest
     * @return UniversalSearchInfo
     */
    @RouteMapping(url = "/search/options", method = "POST")
    public UniversalSearchInfo search(@RequestBody UniversalSearchRequest request) {
        logger.debug("SearchController search  request: {}", request);
        Preconditions.checkNotNull(request, " Invalid parameter `request`: found null object");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(request.getSearchFilterOptions()), " searchFilterOptions is null or is the empty set");
        return searchService.search(request);
    }

    /**
     * search
     *
     * @param searchedInfo
     * @return
     */
    @RouteMapping(url = "/search/update", method = "POST")
    public void saveOrUpdate(@RequestBody SearchedInfo searchedInfo) {
        logger.debug("SearchController saveOrUpdate  request: {}", searchedInfo);
        Preconditions.checkNotNull(searchedInfo, "searchedInfo");
        Preconditions.checkNotNull(searchedInfo.getGid(), "searchedInfo");
        Preconditions.checkNotNull(searchedInfo.getResourceType(), "searchedInfo");
        searchService.saveOrUpdate(searchedInfo);
    }

    /**
     * @param searchedInfo resourceType and gid is not null
     */
    @RouteMapping(url = "/search/remove", method = "POST")
    public void remove(@RequestBody SearchedInfo searchedInfo) {
        searchService.remove(searchedInfo);
    }

}
