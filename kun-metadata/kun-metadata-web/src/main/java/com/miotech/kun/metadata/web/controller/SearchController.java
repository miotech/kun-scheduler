package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.SearchService;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.ResourceAttributeInfoRequest;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import org.apache.commons.lang3.StringUtils;
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
     * @param keyword
     * @return UniversalSearchInfo
     */
    @RouteMapping(url = "/search/full", method = "GET")
    public UniversalSearchInfo fullSearch(@QueryParameter String keyword) {
        logger.debug("SearchController fullTextSearch  keyword: {}", keyword);
        if (StringUtils.isBlank(keyword)) {
            UniversalSearchRequest request = new UniversalSearchRequest();
            return searchService.noneKeywordPage(request);
        }
        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{keyword})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setSearchFilterOptions(searchFilterOptionList);
        request.setPageSize(Integer.MAX_VALUE);
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
        String keywordJoin = request.getSearchFilterOptions()
                .stream().map(SearchFilterOption::getKeyword)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(""));
        logger.debug("search keywordJoin:{}", keywordJoin);
        if (StringUtils.isBlank(keywordJoin)) {
            return searchService.noneKeywordPage(request);
        }
        return searchService.search(request);
    }

    /**
     * @param request ResourceAttributeInfoRequest
     * @return UniversalSearchInfo
     */
    @RouteMapping(url = "/search/attribute/list", method = "POST")
    public List<String> fetchResourceAttributeList(@RequestBody ResourceAttributeInfoRequest request) {
        logger.debug("SearchController fetchResourceAttributeList  request: {}", request);
        return searchService.fetchResourceAttributeList(request);
    }

    /**
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
