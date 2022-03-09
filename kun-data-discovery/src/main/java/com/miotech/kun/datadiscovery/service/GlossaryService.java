package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.persistence.GlossaryRepository;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Service
public class GlossaryService extends BaseSecurityService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GlossaryRepository glossaryRepository;
    @Autowired
    SearchAppService searchAppService;

    public static final String COPY_PREFIX = "Copy of ";


    public Long getParentId(Long id) {
        return glossaryRepository.getParentId(id);
    }

    public Long updateGraph(Long id, GlossaryGraphRequest glossaryGraphRequest) {
        return glossaryRepository.updateGraph(getCurrentUsername(), id, glossaryGraphRequest);
    }

    public List<GlossaryBasicInfo> getGlossariesByDataset(Long datasetGid) {
        if (datasetGid == null) {
            return Lists.newArrayList();
        }
        return glossaryRepository.getGlossariesByDataset(datasetGid);
    }

    public Glossary createGlossary(GlossaryRequest glossaryRequest) {
        Long id = add(glossaryRequest);
        return fetchGlossary(id);
    }

    public Long add(GlossaryRequest glossaryRequest) {
        OffsetDateTime now = DateTimeUtils.now();
        glossaryRequest.setCreateUser(getCurrentUsername());
        glossaryRequest.setCreateTime(now);
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(now);
        Long gid = glossaryRepository.insert(glossaryRequest);
        searchAppService.saveOrUpdateGlossarySearchInfo(getGlossaryBasicInfo(gid));
        return gid;
    }

    public List<GlossaryBasicInfo> fetchChildrenBasicList(Long parentId) {
        return glossaryRepository.findChildren(parentId);
    }

    public GlossaryChildren fetchGlossaryChildren(Long parentId) {
        GlossaryChildren glossaryChildren = new GlossaryChildren();
        glossaryChildren.setParentId(parentId);
        glossaryChildren.setChildren(glossaryRepository.findChildrenCountList(parentId));
        return glossaryChildren;
    }

    public Glossary fetchGlossary(Long id) {
        GlossaryBasicInfo glossaryBasicInfo = getGlossaryBasicInfo(id);
        if (Objects.isNull(glossaryBasicInfo)) {
            throw new IllegalArgumentException("glossary does not exist,id:" + id);
        }

        Glossary glossary = AppBasicConversionService.getSharedInstance().convert(glossaryBasicInfo, Glossary.class);
        if (Objects.nonNull(glossaryBasicInfo.getParentId()) && glossaryBasicInfo.getParentId() != 0) {
            GlossaryBasicInfo parentGlossaryBasicInfo = getGlossaryBasicInfo(glossaryBasicInfo.getParentId());
            glossary.setParent(parentGlossaryBasicInfo);
        }
        List<Long> glossaryToDataSetIdList = glossaryRepository.findGlossaryToDataSetIdList(id);
        glossary.setAssets(findAssets(glossaryToDataSetIdList));
        List<GlossaryBasicInfo> ancestryGlossaryList = glossaryRepository.findAncestryGlossaryList(id);
        glossary.setAncestryGlossaryList(ancestryGlossaryList);
        List<GlossaryBasicInfoWithCount> children = fetchGlossaryChildren(id).getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            glossary.setChildrenCount(children.size());
            glossary.setGlossaryCountList(children);
        }

        return glossary;
    }


    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
        OffsetDateTime now = DateTimeUtils.now();
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(now);
        glossaryRepository.update(id, glossaryRequest);
        Glossary glossary = fetchGlossary(id);
        searchAppService.saveOrUpdateGlossarySearchInfo(glossary);
        return glossary;
    }

    public void delete(Long id) {
        glossaryRepository.delete(getCurrentUsername(), id);
        searchAppService.removeGlossarySearchInfo(id);
    }

    public SearchResult search(GlossaryBasicSearchRequest searchRequest) {
        UniversalSearchInfo universalSearchInfo = searchAppService.fullSearch(searchRequest.getKeyword());
        Stream<SearchedInfo> searchedInfoStream = universalSearchInfo.getSearchedInfoList().stream();
        Long currentId = searchRequest.getCurrentId();
        if (Objects.nonNull(currentId)) {
            List<Long> descendants = glossaryRepository.findSelfDescendants(currentId).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
            searchedInfoStream = searchedInfoStream.filter(searchedInfo -> !descendants.contains(searchedInfo.getGid()));
        }
        List<Long> glossaryIds = searchRequest.getGlossaryIds();
        if (CollectionUtils.isNotEmpty(glossaryIds)) {
            searchedInfoStream = searchedInfoStream.filter(searchedInfo -> glossaryIds.contains(searchedInfo.getGid()));
        }
        List<SearchedInfo> collect = searchedInfoStream.collect(Collectors.toList());
        SearchResult searchResult = new SearchResult();
        searchResult.setSearchedInfoList(collect);
        return searchResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public Glossary copy(Long copyId) {
        GlossaryBasicInfo oldGlossaryBasicInfo = getGlossaryBasicInfo(copyId);
        Long newGlossaryId = copy(oldGlossaryBasicInfo);
        return fetchGlossary(newGlossaryId);
    }

    private Long copy(GlossaryBasicInfo oldGlossaryBasicInfo) {
        Long newGlossaryId = copySelf(oldGlossaryBasicInfo);
        List<GlossaryBasicInfo> oldChildren = fetchChildrenBasicList(oldGlossaryBasicInfo.getId());
        if (CollectionUtils.isNotEmpty(oldChildren)) {
            oldChildren.forEach(glossary -> {
                glossary.setParentId(newGlossaryId);
                copy(glossary);
            });
        }
        return newGlossaryId;
    }


    public GlossaryBasicInfo getGlossaryBasicInfo(Long id) {
        return glossaryRepository.findGlossaryBaseInfo(id);

    }

    private Long copySelf(GlossaryBasicInfo oldGlossaryBasicInfo) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setName(getCopyName(oldGlossaryBasicInfo.getName()));
        glossaryRequest.setParentId(oldGlossaryBasicInfo.getParentId());
        glossaryRequest.setDescription(oldGlossaryBasicInfo.getDescription());
        List<Long> glossaryToDataSetIdList = glossaryRepository.findGlossaryToDataSetIdList(oldGlossaryBasicInfo.getId());
        if (CollectionUtils.isNotEmpty(glossaryToDataSetIdList)) {
            glossaryRequest.setAssetIds(glossaryToDataSetIdList);
        }
        return add(glossaryRequest);
    }

    public static String getCopyName(String oldName) {
        return COPY_PREFIX + oldName;
    }


    private List<Asset> findAssets(List<Long> glossaryToDataSetIdList) {
        if (CollectionUtils.isEmpty(glossaryToDataSetIdList)) {
            return Lists.newArrayList();
        }
        String suggestColumnUrl = url + "/dataset/id_list";


        ParameterizedTypeReference<List<DatasetBasicInfo>> typeRef = new ParameterizedTypeReference<List<DatasetBasicInfo>>() {
        };
        ResponseEntity<List<DatasetBasicInfo>> responseEntity = restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(glossaryToDataSetIdList), typeRef);
        List<DatasetBasicInfo> datasetBasicInfos = responseEntity.getBody();
        return Objects.requireNonNull(datasetBasicInfos)
                .stream()
                .filter(Objects::nonNull)
                .map(datasetBasicInfo -> AppBasicConversionService.getSharedInstance().convert(datasetBasicInfo, Asset.class))
                .collect(Collectors.toList());
    }


}
