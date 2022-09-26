package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.UniversalSearchDao;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.ResourceAttributeInfoRequest;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: search Service
 * @author: zemin  huang
 * @create: 2022-03-08 10:14
 **/
@Singleton
public class SearchService {

    private UniversalSearchDao universalSearchDao;
    private final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Inject
    public SearchService(UniversalSearchDao universalSearchDao) {
        this.universalSearchDao = universalSearchDao;
    }

    public UniversalSearchInfo search(UniversalSearchRequest request) {
        logger.debug("search search:{}", request);
        checked(request);
        Integer searchCount = universalSearchDao.searchCount(request);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo();
        universalSearchInfo.setPageNumber(request.getPageNumber());
        universalSearchInfo.setPageNumber(request.getPageSize());
        if (Objects.isNull(searchCount) || searchCount <= 0) {
            logger.debug("count is 0");
            universalSearchInfo.setTotalCount(0);
            return universalSearchInfo;
        }
        universalSearchInfo.setTotalCount(searchCount);
        List<SearchedInfo> searchResult = universalSearchDao.search(request);
        universalSearchInfo.setSearchedInfoList(searchResult);
        return universalSearchInfo;
    }

    public UniversalSearchInfo noneKeywordPage(UniversalSearchRequest request) {
        logger.debug("search search:{}", request);
        List<SearchedInfo> searchedInfoList = universalSearchDao.noneKeywordPage(request);
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo();
        universalSearchInfo.setPageNumber(request.getPageNumber());
        universalSearchInfo.setPageSize(request.getPageSize());
        universalSearchInfo.setSearchedInfoList(searchedInfoList);
        Integer searchCount = universalSearchDao.noneKeywordSearchCount(request);
        universalSearchInfo.setTotalCount(searchCount);
        return universalSearchInfo;
    }

    public List<String> fetchResourceAttributeList(ResourceAttributeInfoRequest request) {
        ResourceType resourceType = request.getResourceType();
        String attributeName = request.getResourceAttributeName();
        Map<String, Object> attributeMap = request.getResourceAttributeMap();
        Boolean showDeleted = request.getShowDeleted();

        return universalSearchDao.fetchResourceAttributeList(resourceType, attributeName, attributeMap, showDeleted);
    }

    private void checked(UniversalSearchRequest request) {
        List<SearchFilterOption> searchFilterOptions = request.getSearchFilterOptions();
        List<SearchFilterOption> collect = searchFilterOptions.stream()
                .filter(searchFilterOption -> StringUtils.isNotBlank(searchFilterOption.getKeyword()) &&
                        CollectionUtils.isNotEmpty(searchFilterOption.getSearchContents()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            logger.error("search option is  empty or null options:{}", collect);
            throw new IllegalArgumentException("search option is not empty or null");
        }
    }

    public void saveOrUpdate(SearchedInfo searchedInfo) {
        logger.debug("search saveOrUpdate:type:{},id:{},name:{}", searchedInfo.getResourceType(), searchedInfo.getGid(), searchedInfo.getName());
        if (Objects.isNull(universalSearchDao.find(searchedInfo.getResourceType(), searchedInfo.getGid()))) {
            universalSearchDao.save(searchedInfo);
        } else {
            universalSearchDao.update(searchedInfo);
        }

    }

    public void remove(SearchedInfo searchedInfo) {
        logger.debug("search remove:type:{},id:{},name:{}", searchedInfo.getResourceType(), searchedInfo.getGid(), searchedInfo.getName());

        universalSearchDao.remove(searchedInfo.getResourceType(), searchedInfo.getGid());

    }
}
