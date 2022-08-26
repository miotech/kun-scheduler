package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.protobuf.ProtocolStringList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryCopyRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.enums.*;
import com.miotech.kun.datadiscovery.persistence.GlossaryRepository;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.security.common.UserOperation;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp;
import com.miotech.kun.security.facade.rpc.ScopeRole;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
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
    public static final String COPY_PREFIX = "Copy of ";

    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GlossaryRepository glossaryRepository;
    @Autowired
    SearchAppService searchAppService;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private SecurityRpcClient securityRpcClient;
    @Autowired
    private RdmService rdmService;
    @Autowired
    private FilterRuleAppService filterRuleAppService;


    public Long getParentId(Long id) {
        checkAuth(id, GlossaryUserOperation.READ_GLOSSARY);
        return glossaryRepository.getParentId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_UPDATE_GRAPH, args = {"#id", "#glossaryGraphRequest"})
    public Long updateGraph(Long id, GlossaryGraphRequest glossaryGraphRequest) {
        checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY);
        return glossaryRepository.updateGraph(getCurrentUsername(), id, glossaryGraphRequest);
    }

    public List<GlossaryBasicInfo> getGlossariesByDataset(Long datasetGid) {
        if (datasetGid == null) {
            return Lists.newArrayList();
        }
        return glossaryRepository.getGlossariesByDataset(datasetGid);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_CREATE, args = {"#glossaryRequest"})
    public Glossary createGlossary(GlossaryRequest glossaryRequest) {
        Long id = add(glossaryRequest);
        String owner = getCurrentUsername();
        addOwner(id, owner, false);
        return fetchGlossary(id);
    }

    private Long add(GlossaryRequest glossaryRequest) {
        Long parentId = glossaryRequest.getParentId();
        if (Objects.isNull(parentId)) {
            checkAuth(parentId, GlossaryUserOperation.ADD_GLOSSARY);
        }
        checkAuth(parentId, GlossaryUserOperation.EDIT_GLOSSARY_CHILD);
        OffsetDateTime now = DateTimeUtils.now();
        glossaryRequest.setCreateUser(getCurrentUsername());
        glossaryRequest.setCreateTime(now);
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(now);
        if (haveDuplicateName(null, parentId, glossaryRequest.getName())) {
            log.warn("A glossary with the same name is not allowed at the same level  name parent id:{},name:{}", parentId, glossaryRequest.getName());
            throw new IllegalStateException("A glossary with the same name is not allowed at the same level  name:" + glossaryRequest.getName());
        }
        Long gid = glossaryRepository.insert(glossaryRequest);
        addGlossaryResource(gid, glossaryRequest.getAssetIds());
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
        List<GlossaryBasicInfoWithCount> childrenCountList = glossaryRepository.findChildrenCountList(parentId)
                .stream()
                .peek(glossaryBasicInfoWithCount -> glossaryBasicInfoWithCount.setSecurityInfo(fetchGlossaryOperation(glossaryBasicInfoWithCount.getId())))
                .filter(glossaryBasicInfoWithCount -> glossaryBasicInfoWithCount.getSecurityInfo().getOperations().contains(GlossaryUserOperation.READ_GLOSSARY))
                .collect(Collectors.toList());
        glossaryChildren.setChildren(childrenCountList);
        return glossaryChildren;
    }

    public Glossary fetchGlossary(Long id) {
        SecurityInfo securityInfo = checkAuth(id, GlossaryUserOperation.READ_GLOSSARY);
        GlossaryBasicInfo glossaryBasicInfo = getGlossaryBasicInfo(id);
        if (Objects.isNull(glossaryBasicInfo)) {
            throw new IllegalArgumentException("glossary does not exist,id:" + id);
        }

        Glossary glossary = AppBasicConversionService.getSharedInstance().convert(glossaryBasicInfo, Glossary.class);
        glossary.setSecurityInfo(securityInfo);
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

    private Map<Long, List<GlossaryBasicInfo>> findAncestryList(Collection<Long> collectionId) {
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

    public List<GlossaryBasicInfo> findGlossaryList(Collection<Long> ids) {
        return glossaryRepository.findGlossaryList(ids);
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

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_UPDATE, args = {"#id", "#glossaryRequest"})
    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
        checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY);
        GlossaryBasicInfo glossaryBasicInfo = getGlossaryBasicInfo(id);
        if (!Objects.equals(glossaryBasicInfo.getParentId(), glossaryRequest.getParentId())) {
            Long parentId = glossaryRequest.getParentId();
            if (Objects.isNull(parentId)) {
                checkAuth(parentId, GlossaryUserOperation.ADD_GLOSSARY);
            } else {
                checkAuth(parentId, GlossaryUserOperation.EDIT_GLOSSARY_CHILD);
            }
        }
        OffsetDateTime now = DateTimeUtils.now();
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(now);
        if (haveDuplicateName(id, glossaryRequest.getParentId(), glossaryRequest.getName())) {
            log.warn("A glossary with the same name is not allowed at the same level  name parent id:{},name:{}", glossaryRequest.getParentId(), glossaryRequest.getName());
            throw new RuntimeException("A glossary with the same name is not allowed at the same level  name:" + glossaryRequest.getName());
        }
        glossaryRepository.update(id, glossaryRequest);
        updateWholeAssets(id, glossaryRequest.getAssetIds());
        Glossary glossary = fetchGlossary(id);
        searchAppService.saveOrUpdateGlossarySearchInfo(glossary);
        return glossary;
    }

    private void updateWholeAssets(Long id, List<Long> assetIds) {
        List<Long> oldList = glossaryRepository.findGlossaryToDataSetIdList(id);
        Collection<Long> intersection = CollectionUtils.intersection(assetIds, oldList);
        Collection<Long> subtractRemove = CollectionUtils.subtract(oldList, intersection);
        removeGlossaryResource(id, subtractRemove);
        Collection<Long> subtractAdd = CollectionUtils.subtract(assetIds, intersection);
        addGlossaryResource(id, subtractAdd);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_DELETE, args = {"#id"})
    public void delete(Long id) {
        checkAuth(id, GlossaryUserOperation.REMOVE_GLOSSARY);
        String currentUsername = getCurrentUsername();
        List<Long> deleteIdList = glossaryRepository.delete(currentUsername, id);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            deleteIdList.forEach(deleteId -> searchAppService.removeGlossarySearchInfo(deleteId));
        }
    }

    public SearchPage<GlossarySearchedInfo> search(GlossaryBasicSearchRequest searchRequest) {
        UniversalSearchInfo universalSearchInfo = searchAppService.searchGlossary(searchRequest);
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
        return searchedInfoList.stream()
                .map(searchedInfo -> AppBasicConversionService.getSharedInstance().convert(searchedInfo, GlossarySearchedInfo.class))
                .filter(Objects::nonNull)
                .filter(this::canSearch)
                .peek(glossarySearchedInfo -> glossarySearchedInfo.setAncestryGlossaryList(ancestryGlossaryMap.get(glossarySearchedInfo.getGid())))
                .collect(Collectors.toList());
    }

    private boolean canSearch(GlossarySearchedInfo glossarySearchedInfo) {
        SecurityInfo securityInfo = fetchGlossaryOperation(glossarySearchedInfo.getGid());
        return securityInfo.getOperations().contains(GlossaryUserOperation.SEARCH_GLOSSARY);
    }

    /**
     * @param copyReq
     * @return parent glossary
     */

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_COPY_PASTE, args = {"#copyReq"})
    public GlossaryChildren copy(GlossaryCopyRequest copyReq) {
        checkAuth(copyReq.getSourceId(), GlossaryUserOperation.COPY_GLOSSARY);
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
        copyRecursively(parentId, sourceId, Lists.newArrayList(), true);

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
            oldChildren.forEach(glossary -> copyRecursively(parentId, glossary.getId(), newGlossaryList, false));
        }
    }

    private Long copyOnlyOneSelf(Long parentId, Long sourceId) {
        GlossaryBasicInfo oldGlossaryBasicInfo = getGlossaryBasicInfo(sourceId);
        return copySelf(oldGlossaryBasicInfo, parentId, true);
    }

    private void copyRecursively(@Nullable Long parentId, Long sourceId, List<Long> newGlossaryList, boolean replaceName) {
        GlossaryBasicInfo oldGlossaryBasicInfo = getGlossaryBasicInfo(sourceId);
        List<GlossaryBasicInfo> oldChildren = fetchChildrenBasicList(oldGlossaryBasicInfo.getId());
        Long newGlossaryId = copySelf(oldGlossaryBasicInfo, parentId, replaceName);
        newGlossaryList.add(newGlossaryId);
        if (CollectionUtils.isNotEmpty(oldChildren)) {
            Collections.reverse(oldChildren);
            oldChildren.stream()
                    .filter(child -> !newGlossaryList.contains(child.getId()))
                    .forEach(glossary -> copyRecursively(newGlossaryId, glossary.getId(), newGlossaryList, false));
        }
    }


    public GlossaryBasicInfo getGlossaryBasicInfo(Long id) {
        return glossaryRepository.findGlossaryBaseInfo(id);

    }

    private Long copySelf(GlossaryBasicInfo oldGlossaryBasicInfo, Long parentId, boolean replaceName) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        String copyName = oldGlossaryBasicInfo.getName();
        if (replaceName) {
            copyName = getCopyName(oldGlossaryBasicInfo.getName());
        }
        glossaryRequest.setName(copyName);
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


    private SecurityInfo checkAuth(Long sourceSystemId, GlossaryUserOperation glossaryUserOperation) {
        SecurityInfo securityInfo = fetchGlossaryOperation(sourceSystemId);
        Set<UserOperation> operations = securityInfo.getOperations();
        if (CollectionUtils.isNotEmpty(operations) && operations.contains(glossaryUserOperation)) {
            return securityInfo;
        }
        String msg = String.format("You do not have permission to perform this operation：%s,id:%s", sourceSystemId, glossaryUserOperation.name());
        log.error(msg);
        throw new PermissionDeniedDataAccessException(msg, new RuntimeException(msg));
    }

    public SecurityInfo fetchGlossaryOperation(Long id) {
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        SecurityInfo securityInfo = new SecurityInfo();
        securityInfo.setSourceSystemId(id);
        securityInfo.setSecurityModule(securityModule);
        KunRole role = GlossaryRole.GLOSSARY_VIEWER;
        securityInfo.setKunRole(role);
        securityInfo.setOperations(role.getUserOperation());
        KunRole userRole = getUserRole();
        log.debug("userRole:{}", userRole);
//        GLOSSARY_MANAGER :Max permission
        if (userRole.equals(GlossaryRole.GLOSSARY_MANAGER)) {
            role = GlossaryRole.GLOSSARY_MANAGER;
            securityInfo.setKunRole(role);
            securityInfo.setOperations(role.getUserOperation());
            return securityInfo;
        }
        if (Objects.isNull(id)) {
            return securityInfo;
        }
        if (userRole.equals(GlossaryRole.GLOSSARY_EDITOR)) {
            securityInfo.addUserOperation(GlossaryUserOperation.COPY_GLOSSARY);
        }
        List<GlossaryBasicInfo> ancestryList = glossaryRepository.findAncestryList(Lists.newArrayList(id));
        List<String> ancestryIdList = ancestryList.stream().map(glossaryBasicInfo -> glossaryBasicInfo.getId().toString()).collect(Collectors.toList());
        RoleOnSpecifiedResourcesResp roleOnSpecifiedResources = null;
        try {
            roleOnSpecifiedResources = securityRpcClient.findRoleOnSpecifiedResources(securityModule.name(), ancestryIdList);
        } catch (Exception e) {
            log.error("security is access failed", e);
            return securityInfo;
        }
        if (Objects.isNull(roleOnSpecifiedResources)) {
            return securityInfo;
        }
        List<ScopeRole> scopeRolesList = roleOnSpecifiedResources.getScopeRolesList();
        if (CollectionUtils.isEmpty(scopeRolesList)) {
            return securityInfo;
        }
        Map<String, KunRole> roleMap = securityModule.getRoleMap();
        log.debug("scope Roles List:{}", scopeRolesList);
        role = scopeRolesList.stream().map(scopeRole -> roleMap.get(scopeRole.getRolename())).filter(Objects::nonNull).max(Comparator.comparing(KunRole::rank)).orElse(role);
        securityInfo.setKunRole(role);
        Set<UserOperation> userOperations = Sets.newHashSet(role.getUserOperation());
        securityInfo.addUserOperations(userOperations);
        return securityInfo;
    }

    private @NotNull KunRole getUserRole() {
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        RoleOnSpecifiedModuleResp roleOnSpecifiedModule = null;
        try {
            roleOnSpecifiedModule = securityRpcClient.findRoleOnSpecifiedModule(securityModule.name());
        } catch (Exception e) {
            log.error("security is access failed", e);
            return GlossaryRole.GLOSSARY_VIEWER;
        }
        if (Objects.isNull(roleOnSpecifiedModule)) {
            return GlossaryRole.GLOSSARY_VIEWER;
        }
        ProtocolStringList rolenamesList = roleOnSpecifiedModule.getRolenamesList();
        log.debug("rolenamesList:{}", rolenamesList);
        if (CollectionUtils.isEmpty(rolenamesList)) {
            return GlossaryRole.GLOSSARY_VIEWER;
        }
        Map<String, KunRole> roleMap = securityModule.getRoleMap();
        return rolenamesList.stream().map(roleMap::get).max(Comparator.comparing(KunRole::rank)).orElse(GlossaryRole.GLOSSARY_VIEWER);


    }

    private Long addScope(Long id, String userName, GlossaryRole role) {
        String moduleName = SecurityModule.GLOSSARY.name();
        securityRpcClient.addScopeOnSpecifiedRole(moduleName, role.name(), userName, Lists.newArrayList(id.toString()));
        return id;
    }

    private Long removeScope(Long id, String userName, GlossaryRole role) {
        String moduleName = SecurityModule.GLOSSARY.name();
        securityRpcClient.deleteScopeOnSpecifiedRole(moduleName, role.name(), userName, Lists.newArrayList(id.toString()));
        return id;
    }

    public List<String> getGlossaryEditorList(Long id) {
        String moduleName = SecurityModule.GLOSSARY.name();
        String role = GlossaryRole.GLOSSARY_EDITOR.name();
        return securityRpcClient.getGlossaryEditorList(moduleName, role, id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_ADD_OWNER, args = {"#id", "#owner", "#checkAuth"})
    public Long addOwner(Long id, String owner, boolean checkAuth) {
        if (checkAuth) {
            checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY_EDITOR);
        }
        return addScope(id, owner, GlossaryRole.GLOSSARY_EDITOR);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_REMOVE_OWNER, args = {"#id", "#owner", "#checkAuth"})
    public Long removeOwner(Long id, String owner, boolean checkAuth) {
        if (checkAuth) {
            checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY_EDITOR);
        }
        return removeScope(id, owner, GlossaryRole.GLOSSARY_EDITOR);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_ADD_RESOURCE, args = {"#id", "#assetIds"})
    public boolean addGlossaryResource(Long id, Collection<Long> assetIds) {
        checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY_RESOURCE);
        if (CollectionUtils.isEmpty(assetIds)) {
            log.debug("assetIds is empty");
            return true;
        }
        GlossaryBasicInfo glossaryBaseInfo = glossaryRepository.findGlossaryBaseInfo(id);
        if (Objects.isNull(glossaryBaseInfo)) {
            log.error("glossary:{} does not  exist", id);
            throw new IllegalArgumentException(String.format("glossary:%s does not  exist", id));
        }
        List<Long> glossaryToDataSetIdList = glossaryRepository.findGlossaryToDataSetIdList(id);
        List<Long> addList = assetIds.stream().filter(assetId -> !glossaryToDataSetIdList.contains(assetId)).collect(Collectors.toList());
        glossaryRepository.insertGlossaryDatasetRef(id, addList, getCurrentUsername(), DateTimeUtils.now());
        rdmService.addGlossary(id, assetIds);
        addList.forEach(filterRuleAppService::addDatasetStatistics);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.GLOSSARY_REMOVE_RESOURCE, args = {"#id", "#assetIds"})
    public boolean removeGlossaryResource(Long id, Collection<Long> assetIds) {
        checkAuth(id, GlossaryUserOperation.EDIT_GLOSSARY_RESOURCE);
        if (CollectionUtils.isEmpty(assetIds)) {
            log.debug("assetIds is empty");
            return true;
        }
        GlossaryBasicInfo glossaryBaseInfo = glossaryRepository.findGlossaryBaseInfo(id);
        if (Objects.isNull(glossaryBaseInfo)) {
            log.error("glossary:{} does not  exist", id);
            throw new IllegalArgumentException(String.format("glossary:%s does not  exist", id));
        }
        List<Long> glossaryToDataSetIdList = glossaryRepository.findGlossaryToDataSetIdList(id);
        List<Long> removeList = assetIds.stream().filter(glossaryToDataSetIdList::contains).collect(Collectors.toList());
        glossaryRepository.removeGlossaryRef(id, removeList, getCurrentUsername(), DateTimeUtils.now());
        rdmService.removeGlossary(id, assetIds);
        removeList.forEach(filterRuleAppService::removeDatasetStatistics);

        return true;
    }
}
