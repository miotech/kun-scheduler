package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryCopyRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.persistence.GlossaryRepository;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Service
@Slf4j
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
    @Autowired
    private MetadataService metadataService;


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
        if (haveDuplicateName(null, glossaryRequest.getParentId(), glossaryRequest.getName())) {
            log.warn("A glossary with the same name is not allowed at the same level  name parent id:{},name:{}", glossaryRequest.getParentId(), glossaryRequest.getName());
            throw new RuntimeException("A glossary with the same name is not allowed at the same level  name:" + glossaryRequest.getName());
        }
        Long gid = glossaryRepository.insert(glossaryRequest);
        searchAppService.saveOrUpdateGlossarySearchInfo(getGlossaryBasicInfo(gid));
        return gid;
    }

    private boolean haveDuplicateName(Long currentId, Long parentId, String name) {
        List<GlossaryBasicInfo> glossaryBasicInfos = fetchChildrenBasicList(parentId);
        Stream<GlossaryBasicInfo> stream = glossaryBasicInfos.stream();
        if (Objects.nonNull(currentId)) {
            stream = stream.filter(glossaryBasicInfo -> !currentId.equals(glossaryBasicInfo.getId()));
        }
        return stream.map(GlossaryBasicInfo::getName).anyMatch(name::equals);

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

    public Map<Long, List<GlossaryBasicInfo>> findAncestryList(Collection<Long> collectionId) {
        Map<Long, List<GlossaryBasicInfo>> listMap = new HashMap<>();
        List<GlossaryBasicInfo> ancestryList = glossaryRepository.findAncestryList(collectionId);
        Map<Long, GlossaryBasicInfo> map = ancestryList.stream().collect(Collectors.toMap(GlossaryBasicInfo::getId, v -> v, (v1, v2) -> v1));
        for (Long aLong : collectionId) {
            List<GlossaryBasicInfo> glossaryBasicInfoList = new ArrayList<>();
            fillAncestryList(map, aLong, glossaryBasicInfoList);
            Collections.reverse(glossaryBasicInfoList);
            listMap.put(aLong, glossaryBasicInfoList);
        }
        return listMap;
    }


    private void fillAncestryList(Map<Long, GlossaryBasicInfo> map, Long id, List<GlossaryBasicInfo> glossaryBasicInfoList) {
        if (Objects.isNull(id)) {
            return;
        }
        GlossaryBasicInfo glossaryBasicInfo = map.get(id);
        if (Objects.isNull(glossaryBasicInfo)) {
            return;
        }
        glossaryBasicInfoList.add(glossaryBasicInfo);
        Long parentId = glossaryBasicInfo.getParentId();
        fillAncestryList(map, parentId, glossaryBasicInfoList);

    }

    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
        OffsetDateTime now = DateTimeUtils.now();
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(now);
        if (haveDuplicateName(id, glossaryRequest.getParentId(), glossaryRequest.getName())) {
            log.warn("A glossary with the same name is not allowed at the same level  name parent id:{},name:{}", glossaryRequest.getParentId(), glossaryRequest.getName());
            throw new RuntimeException("A glossary with the same name is not allowed at the same level  name:" + glossaryRequest.getName());
        }
        glossaryRepository.update(id, glossaryRequest);
        Glossary glossary = fetchGlossary(id);
        searchAppService.saveOrUpdateGlossarySearchInfo(glossary);
        return glossary;
    }

    public void delete(Long id) {
        glossaryRepository.delete(getCurrentUsername(), id);
        searchAppService.removeGlossarySearchInfo(id);
    }

    public SearchPage<GlossarySearchedInfo> search(GlossaryBasicSearchRequest searchRequest) {
        UniversalSearchInfo universalSearchInfo = searchAppService.searchGlossary(searchRequest.getPageNumber(), searchRequest.getPageSize(), searchRequest.getKeyword());
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
        List<GlossarySearchedInfo> glossarySearchedInfos = getGlossarySearchedInfos(collect);
        SearchPage<GlossarySearchedInfo> searchPage = new SearchPage<>();
        searchPage.setSearchedInfoList(glossarySearchedInfos);
        searchPage.setPageSize(universalSearchInfo.getPageSize());
        searchPage.setPageSize(universalSearchInfo.getPageNumber());
        searchPage.setTotalCount(universalSearchInfo.getTotalCount());
        return searchPage;
    }


    private List<GlossarySearchedInfo> getGlossarySearchedInfos(List<SearchedInfo> searchedInfoList) {
        Set<Long> set = searchedInfoList.stream().map(SearchedInfo::getGid).collect(Collectors.toSet());
        Map<Long, List<GlossaryBasicInfo>> ancestryGlossaryMap = findAncestryList(set);
        return searchedInfoList.stream().map(searchedInfo -> AppBasicConversionService.getSharedInstance().convert(searchedInfo, GlossarySearchedInfo.class)).filter(Objects::nonNull).peek(glossarySearchedInfo -> glossarySearchedInfo.setAncestryGlossaryList(ancestryGlossaryMap.get(glossarySearchedInfo.getGid()))).collect(Collectors.toList());
    }

    /**
     * @param copyReq
     * @return parent glossary
     */

    @Transactional(rollbackFor = Exception.class)
    public GlossaryChildren copy(GlossaryCopyRequest copyReq) {
        log.debug("copy info:{}", copyReq);
        Long parentId = copyReq.getParentId();
        Long sourceId = copyReq.getSourceId();
        switch (copyReq.getCopyOperation()) {
            case ONLY_ONESELF:
                copyOnlyOneSelf(parentId, sourceId);
                break;
            case CONTAINS_CHILDREN:
                copyContainsChildren(parentId, sourceId);
                break;
            case ONLY_CHILDREN:
                copyOnlyChildren(parentId, sourceId);
                break;
            default:
                log.error("copy operation does not exist or null:{}", copyReq.getCopyOperation());
                throw new IllegalArgumentException("copy operation does not exist or null");
        }

        return fetchGlossaryChildren(parentId);
    }

    private void copyContainsChildren(Long parentId, Long sourceId) {
        List<Long> descendants = glossaryRepository.findSelfDescendants(sourceId).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
        if ((!sourceId.equals(parentId)) && descendants.contains(parentId)) {
            log.error("The copied node cannot be a parent node:parentId{},sourceId:{}", parentId, sourceId);
            throw new IllegalStateException("The copied node cannot be a parent node");
        }
        copyRecursively(parentId, sourceId, Lists.newArrayList());

    }

    private void copyOnlyChildren(Long parentId, Long sourceId) {
        List<Long> descendants = glossaryRepository.findSelfDescendants(sourceId).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
        if ((!sourceId.equals(parentId)) && descendants.contains(parentId)) {
            log.error("The copied node cannot be a parent node:parentId{},sourceId:{}", parentId, sourceId);
            throw new IllegalStateException("The copied node cannot be a parent node");
        }
        List<GlossaryBasicInfo> oldChildren = fetchChildrenBasicList(sourceId);
        Collections.reverse(oldChildren);
        ArrayList<Long> newGlossaryList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(oldChildren)) {
            oldChildren.forEach(glossary -> copyRecursively(parentId, glossary.getId(), newGlossaryList));
        }
    }

    private void copyOnlyOneSelf(Long parentId, Long sourceId) {
        GlossaryBasicInfo oldGlossaryBasicInfo = getGlossaryBasicInfo(sourceId);
        copySelf(oldGlossaryBasicInfo, parentId);
    }

    private void copyRecursively(@Nullable Long parentId, Long sourceId, List<Long> newGlossaryList) {
        GlossaryBasicInfo oldGlossaryBasicInfo = getGlossaryBasicInfo(sourceId);
        List<GlossaryBasicInfo> oldChildren = fetchChildrenBasicList(oldGlossaryBasicInfo.getId());
        Long newGlossaryId = copySelf(oldGlossaryBasicInfo, parentId);
        newGlossaryList.add(newGlossaryId);
        if (CollectionUtils.isNotEmpty(oldChildren)) {
            Collections.reverse(oldChildren);
            oldChildren.stream()
                    .filter(child -> !newGlossaryList.contains(child.getId()))
                    .forEach(glossary -> copyRecursively(newGlossaryId, glossary.getId(), newGlossaryList));
        }
    }


    public GlossaryBasicInfo getGlossaryBasicInfo(Long id) {
        return glossaryRepository.findGlossaryBaseInfo(id);

    }

    private Long copySelf(GlossaryBasicInfo oldGlossaryBasicInfo, Long parentId) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setName(getCopyName(oldGlossaryBasicInfo.getName()));
        glossaryRequest.setParentId(parentId);
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
        List<DatasetDetail> datasetBasicInfoList = metadataService.getDatasetDetailList(glossaryToDataSetIdList);
        return Objects.requireNonNull(datasetBasicInfoList).stream().filter(Objects::nonNull).map(datasetBasicInfo -> AppBasicConversionService.getSharedInstance().convert(datasetBasicInfo, Asset.class)).collect(Collectors.toList());
    }


}
