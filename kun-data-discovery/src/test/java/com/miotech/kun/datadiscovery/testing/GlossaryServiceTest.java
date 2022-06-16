package com.miotech.kun.datadiscovery.testing;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.CopyOperation;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryCopyRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.enums.GlossaryRole;
import com.miotech.kun.datadiscovery.model.enums.GlossaryUserOperation;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockGlossaryBasicFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockSecurityRpcClientFactory;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp;
import com.miotech.kun.security.facade.rpc.ScopeRole;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.datadiscovery.testing.mockdata.MockSearchInfoFactory.mockSearchGlossary;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class GlossaryServiceTest extends DataDiscoveryTestBase {


    @Autowired
    GlossaryService glossaryService;


    @BeforeEach
    public void mockRoleManager() {
        RoleOnSpecifiedModuleResp glossaryManagerRole = MockSecurityRpcClientFactory.mockRoleOnSpecifiedModule("test", GlossaryRole.GLOSSARY_MANAGER);
        Mockito.when(securityRpcClient.findRoleOnSpecifiedModule(SecurityModule.GLOSSARY.name())).thenReturn(glossaryManagerRole);
    }

    @Test
    void test_getParentIdIsNull() {
        Long parentId = null;
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Long reParentId = glossaryService.getParentId(glossary.getId());
        assertThat(reParentId, is(parentId));
    }

    @Test
    void test_getParentIdNotNull() {
        Long parentId = null;
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        Long reChildParentId = glossaryService.getParentId(glossaryChild.getId());
        assertThat(reChildParentId, is(glossary.getId()));

    }

    private GlossaryRequest createGlossaryRequestWithParent(Long parentId, String name, ImmutableList<Long> assetList) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setParentId(parentId);
        glossaryRequest.setName(name);
        glossaryRequest.setDescription("test  node");
        glossaryRequest.setAssetIds(assetList);
        return glossaryRequest;
    }

    private GlossaryRequest createGlossaryRequestWithParent(Long parentId, ImmutableList<Long> assetList, String name) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setParentId(parentId);
        glossaryRequest.setName(name);
        glossaryRequest.setDescription("test  node");
        glossaryRequest.setAssetIds(assetList);
        return glossaryRequest;
    }


    @Test
    void test_addGlossary() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(null, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);

        assertThat(glossary, is(notNullValue()));
        assertThat(glossary.getId(), is(notNullValue()));
        assertThat(glossary.getName(), is(glossaryRequest.getName()));
        List<Long> assetIds = glossaryRequest.getAssetIds();
        List<Asset> assets = glossary.getAssets();
        assertThat(assets, is(notNullValue()));
        assertThat(assetIds.size(), is(glossary.getAssets().size()));
        assertTrue(assets.stream().map(Asset::getId).allMatch(assetIds::contains));
    }

    @Test
    void test_getGlossariesByDataset() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(null, "glossary1", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        glossaryService.createGlossary(glossaryRequest);

        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(null, "glossary2", assetList);
        mockDatasetBasicInfoList(glossary2Request.getAssetIds());
        glossaryService.createGlossary(glossary2Request);

        GlossaryRequest glossary3Request = createGlossaryRequestWithParent(null, "glossary3", assetList);
        mockDatasetBasicInfoList(glossary3Request.getAssetIds());
        Glossary glossary3 = glossaryService.createGlossary(glossary3Request);
//        create time desc
        List<GlossaryBasicInfo> glossariesByDataset = glossaryService.getGlossariesByDataset(assetList.get(0));

        assertThat(glossariesByDataset, is(notNullValue()));
        assertThat(glossariesByDataset.size(), is(3));
        GlossaryBasicInfo info1 = glossariesByDataset.get(0);
        assertThat(info1.getName(), is(glossary3.getName()));
        assertThat(info1.getId(), is(glossary3.getId()));
    }

    private void mockDatasetBasicInfoList(List<Long> assetIds) {
        List<DatasetDetail> collect = assetIds.stream().map(id -> getDatasetBasicInfo(id, "testName" + id)).collect(Collectors.toList());
        ResponseEntity<List<DatasetDetail>> responseEntity = new ResponseEntity(collect, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<DatasetDetail>>) any())).thenReturn(responseEntity);
    }


    private DatasetDetail getDatasetBasicInfo(long gid, String name) {
        DatasetDetail datasetBasicInfo1 = new DatasetDetail();
        datasetBasicInfo1.setGid(gid);
        datasetBasicInfo1.setType("dataset");
        datasetBasicInfo1.setName(name);
        datasetBasicInfo1.setDatasource("Hive");
        datasetBasicInfo1.setDatabase("dm");
        datasetBasicInfo1.setOwners(ImmutableList.of("test1", "test2"));
        return datasetBasicInfo1;
    }


    /**
     * null->glossary
     */
    @Test
    void test_update_basic_info() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
//        update name
//        update description
        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();
        glossaryRequestUpdate.setName("updateName");
        glossaryRequestUpdate.setDescription("updateDescription");
        List<Long> assetIds = glossary.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(glossary.getParentId());
        Glossary glossaryUpdate = glossaryService.update(glossary.getId(), glossaryRequestUpdate);
        assertThat(glossary.getUpdateTime(), is(not(glossaryUpdate.getUpdateTime())));
        assertThat(glossaryUpdate.getId(), is(glossary.getId()));
        assertThat(glossaryUpdate.getParentId(), is(glossaryRequestUpdate.getParentId()));
        assertThat(glossaryUpdate.getAssets().size(), is(glossaryRequestUpdate.getAssetIds().size()));
        assertThat(glossaryUpdate.getName(), is(glossaryRequestUpdate.getName()));
        assertThat(glossaryUpdate.getDescription(), is(glossaryRequestUpdate.getDescription()));

    }

    @Test
    void test_update_assetList() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
//        update asset
        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();
        glossaryRequestUpdate.setName(glossary.getName());
        glossaryRequestUpdate.setDescription(glossary.getDescription());
        glossaryRequestUpdate.setParentId(glossary.getParentId());
        List<Long> assetIds = glossary.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        assetIds.add(4L);
        assetIds.add(5L);
        assetIds.remove(assetList.get(0));
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(glossary.getParentId());
        Glossary glossaryUpdate = glossaryService.update(glossary.getId(), glossaryRequestUpdate);
        assertTrue(glossaryUpdate.getAssets().stream().map(Asset::getId).anyMatch(assetIds::contains));


    }

    /**
     * null --->glossary->glossaryChild
     * ｜
     * --->glossary2
     */
    @Test
    void test_update_basic_move_parent_down() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary1", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(parentId, "glossary2", assetList);
        Glossary glossary2 = glossaryService.createGlossary(glossary2Request);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);

        /**
         * result
         *null --->glossary----->glossary2
         *                  ｜
         *                   --->glossaryChild
         *
         */
        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();
        glossaryRequestUpdate.setName(glossary2.getName());
        glossaryRequestUpdate.setDescription(glossary2.getDescription());
        List<Long> assetIds = glossary2.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(glossary.getId());
        Glossary updateGlossary2 = glossaryService.update(glossary2.getId(), glossaryRequestUpdate);
        assertThat(updateGlossary2.getParentId(), is(glossary.getId()));
        assertThat(updateGlossary2.getPrevId(), is(nullValue()));
        GlossaryBasicInfo glossaryChildInfo = glossaryService.getGlossaryBasicInfo(glossaryChild.getId());
        assertThat(glossaryChildInfo.getPrevId(), is(glossary2.getId()));
        GlossaryChildren nullGlossaryChildren = glossaryService.fetchGlossaryChildren(null);
        assertThat(nullGlossaryChildren.getChildren().size(), is(1));
        GlossaryChildren glossary1Children = glossaryService.fetchGlossaryChildren(glossary.getId());
        assertThat(glossary1Children.getChildren().size(), is(2));
        assertThat(glossary1Children.getChildren().get(0).getId(), is(glossary2.getId()));
        assertThat(glossary1Children.getChildren().get(1).getId(), is(glossaryChild.getId()));
    }

    /**
     * null --->glossary->glossaryChild------->glossarySon3
     * ｜                                ｜----> glossarySon2
     * --->glossary2                     ｜----> glossarySon1
     * glossarySon2  parent glossaryChild->glossary2
     */
    @Test
    void test_update_throw_Exception() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary1", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon1", assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        GlossaryRequest glossaryRequestSon2 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon2", assetList);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon2);
        GlossaryRequest glossaryRequestSon3 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon3", assetList);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon3);

        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();

        glossaryRequestUpdate.setName(glossaryChild.getName());
        glossaryRequestUpdate.setDescription(glossaryChild.getDescription());
        List<Long> assetIds = glossaryChild.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(glossaryChild.getId());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> glossaryService.update(glossaryChild.getId(), glossaryRequestUpdate));
        assertEquals(String.format("new parent id  should not be id or  Descendants id,parent id:%s,id:%s", glossaryChild.getId(), glossaryChild.getId()), ex.getMessage());
        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> glossaryService.update(glossary.getId(), glossaryRequestUpdate));
        assertEquals(String.format("new parent id  should not be id or  Descendants id,parent id:%s,id:%s", glossaryChild.getId(), glossary.getId()), ex1.getMessage());
    }

    /**
     * null --->glossary->glossaryChild------->glossarySon3
     * ｜                                ｜----> glossarySon2
     * ｜----> glossarySon1
     * glossarySon2  parent glossaryChild->glossary2
     */
    @Test
    void test_delete() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(parentId, "glossary2", assetList);
        Glossary glossary2 = glossaryService.createGlossary(glossary2Request);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon1", assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        GlossaryRequest glossaryRequestSon2 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon2", assetList);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon2);
        GlossaryRequest glossaryRequestSon3 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon3", assetList);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon3);


//        test son node
        glossaryService.delete(glossarySon3.getId());
        Exception ex = assertThrows(RuntimeException.class, () -> glossaryService.fetchGlossary(glossarySon3.getId()));
        assertEquals(String.format("glossary  deleted,id:%s", glossarySon3.getId()), ex.getMessage());


        //        test son node
        glossaryService.delete(glossaryChild.getId());
        Exception ex1 = assertThrows(RuntimeException.class, () -> glossaryService.fetchGlossary(glossaryChild.getId()));
        assertEquals(String.format("glossary  deleted,id:%s", glossaryChild.getId()), ex1.getMessage());
        Exception ex2 = assertThrows(RuntimeException.class, () -> glossaryService.fetchGlossary(glossarySon1.getId()));
        assertEquals(String.format("glossary  deleted,id:%s", glossarySon1.getId()), ex2.getMessage());
        Exception ex3 = assertThrows(RuntimeException.class, () -> glossaryService.fetchGlossary(glossarySon2.getId()));
        assertEquals(String.format("glossary  deleted,id:%s", glossarySon2.getId()), ex3.getMessage());
        Glossary glossary1 = glossaryService.fetchGlossary(glossary.getId());
        assertThat(glossary1, is(notNullValue()));


    }

    /**
     * null --->glossary->glossaryChild------->glossarySon3
     * ｜                           ｜----> glossarySon2
     * --->glossary2                ｜----> glossarySon1
     * glossarySon2  parent glossaryChild->glossary2
     */
    @Test
    void test_update_basic_move_parent_up() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(parentId, "glossary2", assetList);
        Glossary glossary2 = glossaryService.createGlossary(glossary2Request);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon1", assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        GlossaryRequest glossaryRequestSon2 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon2", assetList);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon2);
        GlossaryRequest glossaryRequestSon3 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon3", assetList);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon3);

        GlossaryChildren glossaryChChildrenBefore = glossaryService.fetchGlossaryChildren(glossaryChild.getId());

        assertThat(glossaryChChildrenBefore.getChildren().size(), is(3));
        assertThat(glossaryChChildrenBefore.getChildren().get(0).getId(), is(glossarySon3.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(0).getPrevId(), is(nullValue()));
        assertThat(glossaryChChildrenBefore.getChildren().get(1).getId(), is(glossarySon2.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(1).getPrevId(), is(glossarySon3.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(2).getId(), is(glossarySon1.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(2).getPrevId(), is(glossarySon2.getId()));
        /**
         * result
         *null ----->glossary->glossaryChild------->glossarySon3
         *      ｜                             |----> glossarySon1
         *       --->glossary2----> glossarySon2
         *   glossarySon2  parent glossaryChild->glossary2
         */
        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();
        glossaryRequestUpdate.setName(glossarySon2.getName());
        glossaryRequestUpdate.setDescription(glossarySon2.getDescription());
        List<Long> assetIds = glossarySon2.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(glossary2.getId());
        Glossary glossarySon2Update = glossaryService.update(glossarySon2.getId(), glossaryRequestUpdate);
        assertThat(glossarySon2Update.getParentId(), is(glossary2.getId()));
        assertThat(glossarySon2Update.getPrevId(), is(nullValue()));
        assertThat(glossarySon2Update.getId(), is(glossarySon2.getId()));
        assertThat(glossarySon2Update.getAncestryGlossaryList().size(), is(1));
        GlossaryChildren glossaryChChildrenAfter = glossaryService.fetchGlossaryChildren(glossaryChild.getId());
        assertThat(glossaryChChildrenAfter.getChildren().size(), is(2));
        assertThat(glossaryChChildrenAfter.getChildren().get(0).getId(), is(glossarySon3.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(0).getPrevId(), is(nullValue()));
        assertThat(glossaryChChildrenAfter.getChildren().get(1).getId(), is(glossarySon1.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(1).getPrevId(), is(glossarySon3.getId()));
    }

    /**
     * null --->glossary2->glossaryChild------->glossarySon3
     * ｜                           ｜----> glossarySon2
     * --->glossary                ｜----> glossarySon1
     */
    @Test
    void test_update_basic_move_parent_null() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(parentId, "glossary2", assetList);
        Glossary glossary2 = glossaryService.createGlossary(glossary2Request);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon1", assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        GlossaryRequest glossaryRequestSon2 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon2", assetList);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon2);
        GlossaryRequest glossaryRequestSon3 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon3", assetList);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon3);
        GlossaryChildren glossaryChChildrenBefore = glossaryService.fetchGlossaryChildren(null);

        assertThat(glossaryChChildrenBefore.getChildren().size(), is(2));
        assertThat(glossaryChChildrenBefore.getChildren().get(0).getId(), is(glossary2.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(0).getPrevId(), is(nullValue()));
        assertThat(glossaryChChildrenBefore.getChildren().get(1).getId(), is(glossary.getId()));
        assertThat(glossaryChChildrenBefore.getChildren().get(1).getPrevId(), is(glossary2.getId()));
        /**
         * result
         *null ----->glossary2->glossaryChild------->glossarySon3
         *      ｜                             |----> glossarySon1
         *      ｜ --->glossary
         *      ｜----> glossarySon2
         *   glossarySon2  parent glossaryChild->null
         */
        GlossaryRequest glossaryRequestUpdate = new GlossaryRequest();
        glossaryRequestUpdate.setName(glossarySon2.getName());
        glossaryRequestUpdate.setDescription(glossarySon2.getDescription());
        List<Long> assetIds = glossarySon2.getAssets().stream().map(Asset::getId).collect(Collectors.toList());
        glossaryRequestUpdate.setAssetIds(assetIds);
        glossaryRequestUpdate.setParentId(null);
        Glossary glossarySon2Update = glossaryService.update(glossarySon2.getId(), glossaryRequestUpdate);
        assertThat(glossarySon2Update.getParentId(), is(nullValue()));
        assertThat(glossarySon2Update.getPrevId(), is(nullValue()));
        assertThat(glossarySon2Update.getId(), is(glossarySon2.getId()));
        assertThat(glossarySon2Update.getAncestryGlossaryList().size(), is(0));
        GlossaryChildren glossaryChChildrenAfter = glossaryService.fetchGlossaryChildren(null);
        assertThat(glossaryChChildrenAfter.getChildren().size(), is(3));
        assertThat(glossaryChChildrenAfter.getChildren().get(0).getId(), is(glossarySon2.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(0).getPrevId(), is(nullValue()));
        assertThat(glossaryChChildrenAfter.getChildren().get(1).getId(), is(glossary2.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(1).getPrevId(), is(glossarySon2.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(2).getId(), is(glossary.getId()));
        assertThat(glossaryChChildrenAfter.getChildren().get(2).getPrevId(), is(glossary2.getId()));

    }

    @Test
    void test_copy_ONLY_ONESELF_same_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy only oneself g2-1 到1-1 same level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.ONLY_ONESELF;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getParentId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getParentId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(2));
        GlossaryBasicInfo glossaryCopy = children.get(0);
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossary2_1.getName())));
        assertThat(glossaryCopy.getDescription(), is(glossary2_1.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary2_1.getId()));
        assertThat(glossaryCopy.getParentId(), is(glossary1_1.getParentId()));
        assertThat(children.get(1).getId(), is(glossary1_1.getId()));
        GlossaryChildren glossaryChildren1 = glossaryService.fetchGlossaryChildren(glossaryCopy.getId());
        List<GlossaryBasicInfoWithCount> children1 = glossaryChildren1.getChildren();
        assertTrue(CollectionUtils.isEmpty(children1));

    }

    @Test
    void test_copy_ONLY_ONESELF_children_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy only oneself g2-1 到1-1 children level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.ONLY_ONESELF;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(2));
        Long copyId = children.get(0).getId();
        Glossary glossaryCopy = glossaryService.fetchGlossary(copyId);
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossary2_1.getName())));
        assertThat(glossaryCopy.getDescription(), is(glossary2_1.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary2_1.getId()));
        assertThat(glossaryCopy.getAssets(), is(glossary2_1.getAssets()));
        assertThat(glossaryCopy.getParentId(), is(glossary1_1.getId()));
        assertThat(children.get(1).getId(), is(glossary2_1.getId()));

        GlossaryChildren glossaryChildren2 = glossaryService.fetchGlossaryChildren(glossaryCopy.getId());
        List<GlossaryBasicInfoWithCount> children2 = glossaryChildren2.getChildren();
        assertTrue(CollectionUtils.isEmpty(children2));


    }

    @Test
    void test_copy_CONTAINS_CHILDREN_same_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy contains children  g2-1 到1-1 same level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.CONTAINS_CHILDREN;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getParentId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getParentId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(2));

        assertThat(children.get(1).getId(), is(glossary1_1.getId()));
        Long copy2_1_Id = children.get(0).getId();
        Glossary glossaryCopy = glossaryService.fetchGlossary(copy2_1_Id);
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossary2_1.getName())));
        assertThat(glossaryCopy.getDescription(), is(glossary2_1.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary2_1.getId()));
        assertThat(glossaryCopy.getAssets(), is(glossary2_1.getAssets()));
        assertThat(glossaryCopy.getParentId(), is(glossary1_1.getParentId()));

        GlossaryChildren glossaryChildren1 = glossaryService.fetchGlossaryChildren(copy2_1_Id);
        List<GlossaryBasicInfoWithCount> children1 = glossaryChildren1.getChildren();
        assertThat(children1.size(), is(2));
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        assertThat(children1.get(0).getName(), is(glossary3_2.getName()));
        assertThat(children1.get(1).getName(), is(glossary3_1.getName()));


    }

    @Test
    void test_copy_CONTAINS_CHILDREN_children_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy contains children  g2-1 到1-1 children level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.CONTAINS_CHILDREN;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(2));
        assertThat(children.get(1).getId(), is(glossary2_1.getId()));
        Long copy2_1_Id = children.get(0).getId();
        Glossary glossaryCopy = glossaryService.fetchGlossary(copy2_1_Id);
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossary2_1.getName())));
        assertThat(glossaryCopy.getDescription(), is(glossary2_1.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary2_1.getId()));
        assertThat(glossaryCopy.getAssets(), is(glossary2_1.getAssets()));
        assertThat(glossaryCopy.getParentId(), is(glossary2_1.getParentId()));

        GlossaryChildren glossaryChildren1 = glossaryService.fetchGlossaryChildren(null);
        List<GlossaryBasicInfoWithCount> children1 = glossaryChildren1.getChildren();
        assertThat(children1.size(), is(1));
        assertThat(children1.get(0).getId(), is(glossary1_1.getId()));

        GlossaryChildren glossaryChildren2 = glossaryService.fetchGlossaryChildren(glossaryCopy.getId());
        List<GlossaryBasicInfoWithCount> children2 = glossaryChildren2.getChildren();
        assertThat(children2.size(), is(2));
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        assertThat(children2.get(0).getName(), is(glossary3_2.getName()));
        assertThat(children2.get(1).getName(), is(glossary3_1.getName()));


    }

    @Test
    void test_copy_ONLY_CHILDREN_same_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy only children  g2-1 到1-1 same level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.ONLY_CHILDREN;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getParentId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getParentId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(3));
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        assertThat(children.get(0).getName(), is(glossary3_2.getName()));
        assertThat(children.get(0).getParentId(), not((glossary3_2.getParentId())));
        assertThat(children.get(1).getName(), is(glossary3_1.getName()));
        assertThat(children.get(1).getParentId(), not((glossary3_1.getParentId())));
        assertThat(children.get(2).getName(), is(glossary1_1.getName()));
        Long copy3_2_id = children.get(0).getId();
        Glossary glossaryCopy = glossaryService.fetchGlossary(copy3_2_id);
        assertThat(glossaryCopy.getName(), is(glossary3_2.getName()));
        assertThat(glossaryCopy.getDescription(), is(glossary3_2.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary3_2.getId()));
        assertThat(glossaryCopy.getAssets(), is(glossary3_2.getAssets()));
        assertThat(glossaryCopy.getParentId(), not(glossary3_2.getParentId()));
        assertThat(glossaryCopy.getParentId(), is(nullValue()));
    }

    @Test
    void test_copy_CONTAINS_CHILDREN_throw() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy contains children  g2-1 到1-1 same level
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.CONTAINS_CHILDREN;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary2_1.getId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        GlossaryCopyRequest copyReq1 = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary3_1.getId(), glossary2_1.getId(), copyOperation);
        assertThrows(IllegalStateException.class, () -> glossaryService.copy(copyReq1));
        CopyOperation copyOperation_ONLY_CHILDREN = CopyOperation.ONLY_CHILDREN;
        GlossaryCopyRequest copyReq2 = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary2_1.getId(), glossary2_1.getId(), copyOperation_ONLY_CHILDREN);
//        same name
        assertThrows(IllegalStateException.class, () -> glossaryService.copy(copyReq2));
        GlossaryCopyRequest copyReq3 = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary3_1.getId(), glossary2_1.getId(), copyOperation_ONLY_CHILDREN);
        assertThrows(IllegalStateException.class, () -> glossaryService.copy(copyReq3));
    }

    @Test
    void test_copy_ONLY_CHILDREN_children_level() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
//        copy only children  g2-1 到1-1 same level
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Glossary glossary2_1 = glossaryTree.get("glossary2_1");
        CopyOperation copyOperation = CopyOperation.ONLY_CHILDREN;
        GlossaryCopyRequest copyReq = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary1_1.getId(), glossary2_1.getId(), copyOperation);
        GlossaryChildren glossaryChildren = glossaryService.copy(copyReq);
        assertThat(glossaryChildren, is(notNullValue()));
        assertThat(glossaryChildren.getParentId(), is(glossary1_1.getId()));
        assertThat(glossaryChildren.getChildren(), is(notNullValue()));
        List<GlossaryBasicInfoWithCount> children = glossaryChildren.getChildren();
        assertThat(children.size(), is(3));
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        assertThat(children.get(0).getName(), is(glossary3_2.getName()));
        assertThat(children.get(1).getName(), is(glossary3_1.getName()));
        assertThat(children.get(2).getName(), is(glossary2_1.getName()));
        Long copy3_2_id = children.get(0).getId();
        Glossary glossaryCopy = glossaryService.fetchGlossary(copy3_2_id);

        assertThat(glossaryCopy.getName(), is(glossary3_2.getName()));
        assertThat(glossaryCopy.getDescription(), is(glossary3_2.getDescription()));
        assertThat(glossaryCopy.getId(), not(glossary3_2.getId()));
        assertThat(glossaryCopy.getAssets(), is(glossary3_2.getAssets()));
        assertThat(glossaryCopy.getParentId(), is(glossary1_1.getId()));

    }


    @Test
    public void test_fetchChildrenBasicList_parent_null() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        List<GlossaryBasicInfo> glossaryBasicChildInfoList = glossaryService.fetchChildrenBasicList(parentId);
        assertThat(glossaryBasicChildInfoList.size(), is(1));
        GlossaryBasicInfo son1 = glossaryBasicChildInfoList.get(0);
        assertThat(son1.getId(), is(glossary.getId()));
        assertThat(son1.getName(), is(glossary.getName()));
        assertThat(son1.getParentId(), is(glossary.getParentId()));
    }

    @Test
    public void test_fetchGlossary_AncestryGlossaryList() {
        /**
         *null->glossary->glossaryChild--->glossarySon1
         *                           ｜
         *                           ｜---> glossarySon2
         */
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, "glossary", assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), "glossaryChild", assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon1", assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        GlossaryRequest glossaryRequestSon2 = createGlossaryRequestWithParent(glossaryChild.getId(), "glossarySon2", assetList);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon2);

        List<GlossaryBasicInfo> ancestryGlossaryList = glossarySon2.getAncestryGlossaryList();
        assertThat(ancestryGlossaryList.size(), is(2));
        assertThat(ancestryGlossaryList.get(0).getId(), is(glossary.getId()));
        assertThat(ancestryGlossaryList.get(0).getName(), is(glossary.getName()));
        assertThat(ancestryGlossaryList.get(1).getId(), is(glossaryChild.getId()));
        assertThat(ancestryGlossaryList.get(1).getName(), is(glossaryChild.getName()));


    }

    @Test
    public void test_getGlossaryChild() {
//        load data
        /**
         *null->glossary1(1,2,3)->glossary2(4,5,6)--->glossary3(7,8)
         *                                         ｜
         *                                         ｜---->glossary4(1,5,8)
         */

        ImmutableList<Long> assetList1 = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest1 = createGlossaryRequestWithParent(parentId, "glossary1", assetList1);
        mockDatasetBasicInfoList(glossaryRequest1.getAssetIds());
        Glossary glossary1 = glossaryService.createGlossary(glossaryRequest1);

        ImmutableList<Long> assetList2 = ImmutableList.of(4L, 5L, 6L);
        GlossaryRequest glossaryRequest2 = createGlossaryRequestWithParent(glossary1.getId(), "glossary2", assetList2);
        mockDatasetBasicInfoList(glossaryRequest2.getAssetIds());
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest2);

        ImmutableList<Long> assetList3 = ImmutableList.of(7L, 8L);
        GlossaryRequest glossaryRequest3 = createGlossaryRequestWithParent(glossary2.getId(), "glossarySon1", assetList3);
        mockDatasetBasicInfoList(glossaryRequest3.getAssetIds());
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequest3);

        ImmutableList<Long> assetList4 = ImmutableList.of(1L, 5L, 8L);
        GlossaryRequest glossaryRequest4 = createGlossaryRequestWithParent(glossary2.getId(), "glossarySon2", assetList4);
        mockDatasetBasicInfoList(glossaryRequest4.getAssetIds());
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequest4);
//        testing
        GlossaryChildren glossaryNull1Child = glossaryService.fetchGlossaryChildren(parentId);
        assertThat(glossaryNull1Child.getParentId(), is(parentId));
        List<GlossaryBasicInfoWithCount> glossaryBasicList = glossaryNull1Child.getChildren();
        assertThat(glossaryBasicList.size(), is(1));
        GlossaryBasicInfoWithCount glossaryBasic2 = glossaryBasicList.get(0);
        assertThat(glossaryBasic2.getName(), is(glossary1.getName()));
        assertThat(glossaryBasic2.getChildrenCount(), is(1));
        HashSet<Long> depthDataset1 = Sets.newHashSet(assetList1);
        depthDataset1.addAll(assetList2);
        depthDataset1.addAll(assetList3);
        depthDataset1.addAll(assetList4);
        assertThat(glossaryBasic2.getDataSetCount(), is(depthDataset1.size()));

        GlossaryChildren glossary1Child = glossaryService.fetchGlossaryChildren(glossary1.getId());
        assertThat(glossary1Child.getParentId(), is(glossary1.getId()));
        List<GlossaryBasicInfoWithCount> glossary1BasicList = glossary1Child.getChildren();
        assertThat(glossary1BasicList.size(), is(1));
        GlossaryBasicInfoWithCount glossary1Basic1 = glossary1BasicList.get(0);
        assertThat(glossary1Basic1.getName(), is(glossary2.getName()));
        assertThat(glossary1Basic1.getChildrenCount(), is(2));
        HashSet<Long> depthDataset2 = Sets.newHashSet(assetList2);
        depthDataset2.addAll(assetList3);
        depthDataset2.addAll(assetList4);
        assertThat(glossary1Basic1.getDataSetCount(), is(depthDataset2.size()));

        GlossaryChildren glossary2Child = glossaryService.fetchGlossaryChildren(glossary2.getId());
        assertThat(glossary2Child.getParentId(), is(glossary2.getId()));
        List<GlossaryBasicInfoWithCount> glossary2BasicList = glossary2Child.getChildren();
        assertThat(glossary2BasicList.size(), is(2));
        GlossaryBasicInfoWithCount glossary2Basic1 = glossary2BasicList.get(0);
        assertThat(glossary2Basic1.getName(), is(glossarySon2.getName()));
        assertThat(glossary2Basic1.getChildrenCount(), is(0));
        assertThat(glossary2Basic1.getDataSetCount(), is(assetList4.size()));
    }

    /**
     * null->glossary>glossaryChild--->glossarySon1
     * ｜
     * ｜---> glossarySon2
     */
    private Map<String, Glossary> mockTreeMap(String name1_1, String name2_1, String name3_1, String name3_2) {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Map<String, Glossary> glossaries = Maps.newLinkedHashMap();
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList, name1_1);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        glossaries.put(glossary.getName(), glossary);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList, name2_1);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        glossaries.put(glossaryChild.getName(), glossaryChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList, name3_1);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        glossaries.put(glossarySon1.getName(), glossarySon1);
        GlossaryRequest glossaryRequestSon1 = createGlossaryRequestWithParent(glossaryChild.getId(), assetList, name3_2);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon1);
        glossaries.put(glossarySon2.getName(), glossarySon2);
        return glossaries;
    }

    @Test
    public void test_search_filter_ids() {
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        Glossary glossary = glossaries.get("glossary");
        Glossary glossarySon1 = glossaries.get("glossarySon1");
        String keyword = "glossary";
        UniversalSearchInfo universalSearchInfo = mockSearchGlossary(glossaries.values(), keyword);
        ResponseEntity<UniversalSearchInfo> responseEntity2 = new ResponseEntity(universalSearchInfo, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity2);
        GlossaryBasicSearchRequest basicSearchRequest = new GlossaryBasicSearchRequest();
        basicSearchRequest.setKeyword(keyword);
        ArrayList<Long> longs2 = Lists.newArrayList(glossary.getId(), glossarySon1.getId());
        basicSearchRequest.setGlossaryIds(longs2);
        SearchPage<GlossarySearchedInfo> searchPage = glossaryService.search(basicSearchRequest);
        List<GlossarySearchedInfo> searchedInfoList = searchPage.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(longs2.size()));
        assertTrue(searchedInfoList.stream().map(GlossarySearchedInfo::getGid).anyMatch(longs2::contains));
    }

    @Test
    public void test_search_simple() {
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        Glossary glossaryChild = glossaries.get("glossaryChild");
        String keyword1 = "ch";
        GlossaryBasicSearchRequest basicSearchRequest1 = new GlossaryBasicSearchRequest();
        basicSearchRequest1.setKeyword(keyword1);
        UniversalSearchInfo universalSearchInfo = mockSearchGlossary(glossaries.values(), keyword1);
        ResponseEntity<UniversalSearchInfo> responseEntity = new ResponseEntity(universalSearchInfo, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        SearchPage<GlossarySearchedInfo> searchPage1 = glossaryService.search(basicSearchRequest1);
        List<GlossarySearchedInfo> searchedInfoList = searchPage1.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        assertThat(searchedInfoList.get(0).getGid(), is(glossaryChild.getId()));
    }

    @Test
    public void test_filter_no_self() {
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        Glossary glossaryChild = glossaries.get("glossaryChild");
        String keyword3 = "Child";
        GlossaryBasicSearchRequest basicSearchRequest3 = new GlossaryBasicSearchRequest();
        basicSearchRequest3.setKeyword(keyword3);
        UniversalSearchInfo universalSearchInfo3 = mockSearchGlossary(glossaries.values(), keyword3);
        ResponseEntity<UniversalSearchInfo> responseEntity3 = new ResponseEntity(universalSearchInfo3, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity3);
        basicSearchRequest3.setCurrentId(glossaryChild.getId());
        SearchPage<GlossarySearchedInfo> searchPage3 = glossaryService.search(basicSearchRequest3);
        List<GlossarySearchedInfo> searchedInfoList3 = searchPage3.getSearchedInfoList();
        assertThat(searchedInfoList3.size(), is(0));
    }

    @Test
    public void test_filter_no_child() {
        //        不会包含当前节点和当前节点下的子节点
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        Glossary glossaryChild = glossaries.get("glossaryChild");
        Glossary glossary = glossaries.get("glossary");
        String keyword4 = "glossary";
        GlossaryBasicSearchRequest basicSearchRequest4 = new GlossaryBasicSearchRequest();
        basicSearchRequest4.setKeyword(keyword4);
        UniversalSearchInfo universalSearchInfo4 = mockSearchGlossary(glossaries.values(), keyword4);
        ResponseEntity<UniversalSearchInfo> responseEntity4 = new ResponseEntity(universalSearchInfo4, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity4);
        basicSearchRequest4.setCurrentId(glossaryChild.getId());
        SearchPage<GlossarySearchedInfo> searchPage4 = glossaryService.search(basicSearchRequest4);
        List<GlossarySearchedInfo> searchedInfoList4 = searchPage4.getSearchedInfoList();
        assertThat(searchedInfoList4.size(), is(1));
        assertThat(searchedInfoList4.get(0).getGid(), is(glossary.getId()));
    }

    @Test
    public void test_search_contains_ancestry_list() {
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        String keyword = "glossary";
        UniversalSearchInfo universalSearchInfo = mockSearchGlossary(glossaries.values(), keyword);
        ResponseEntity<UniversalSearchInfo> responseEntity2 = new ResponseEntity(universalSearchInfo, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity2);
        GlossaryBasicSearchRequest basicSearchRequest = new GlossaryBasicSearchRequest();
        basicSearchRequest.setKeyword(keyword);

        SearchPage<GlossarySearchedInfo> searchPage = glossaryService.search(basicSearchRequest);

        List<GlossarySearchedInfo> searchedInfoList = searchPage.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(glossaries.size()));
        Map<String, GlossarySearchedInfo> collect = searchedInfoList.stream().collect(Collectors.toMap(GlossarySearchedInfo::getName, v -> v));
        GlossarySearchedInfo glossary = collect.get("glossary");
        assertThat(glossary.getAncestryGlossaryList().size(), is(1));
        assertThat(glossary.getAncestryGlossaryList().get(0).getId(), is(glossaries.get("glossary").getId()));
        GlossarySearchedInfo glossaryChild = collect.get("glossaryChild");
        assertThat(glossaryChild.getAncestryGlossaryList().size(), is(2));
        assertThat(glossaryChild.getAncestryGlossaryList().get(0).getId(), is(glossaries.get("glossary").getId()));
        assertThat(glossaryChild.getAncestryGlossaryList().get(1).getId(), is(glossaries.get("glossaryChild").getId()));
        GlossarySearchedInfo glossarySon1 = collect.get("glossarySon1");
        assertThat(glossarySon1.getAncestryGlossaryList().size(), is(3));
        assertThat(glossarySon1.getAncestryGlossaryList().get(0).getId(), is(glossaries.get("glossary").getId()));
        assertThat(glossarySon1.getAncestryGlossaryList().get(1).getId(), is(glossaries.get("glossaryChild").getId()));
        assertThat(glossarySon1.getAncestryGlossaryList().get(2).getId(), is(glossaries.get("glossarySon1").getId()));
        GlossarySearchedInfo glossarySon2 = collect.get("glossarySon2");
        assertThat(glossarySon2.getAncestryGlossaryList().size(), is(3));
        assertThat(glossarySon2.getAncestryGlossaryList().get(0).getId(), is(glossaries.get("glossary").getId()));
        assertThat(glossarySon2.getAncestryGlossaryList().get(1).getId(), is(glossaries.get("glossaryChild").getId()));
        assertThat(glossarySon2.getAncestryGlossaryList().get(2).getId(), is(glossaries.get("glossarySon2").getId()));
    }

    @Test
    public void test_haveDuplicateName() {
        Map<String, Glossary> glossaries = mockTreeMap("glossary", "glossaryChild", "glossarySon1", "glossarySon2");
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
//       create
        String name = "glossary";
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(null, assetList, name);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        assertThrows("A glossary with the same name is not allowed at the same level  name:" + name, RuntimeException.class, () -> glossaryService.createGlossary(glossaryRequest));
//       udpate
        GlossaryRequest glossaryUpdateRequest = createGlossaryRequestWithParent(null, assetList, name);
        glossaryUpdateRequest.setDescription("test update");
        Glossary glossary = glossaries.get(name);
        Glossary glossaryUpdate = glossaryService.update(glossary.getId(), glossaryUpdateRequest);
        assertThat(glossaryUpdate.getId(), is(glossary.getId()));

        Glossary glossarySon2 = glossaries.get("glossarySon2");
        String newName = "glossarySon1";//exist
        GlossaryRequest glossaryUpdateRequest2 = createGlossaryRequestWithParent(glossarySon2.getParentId(), assetList, newName);
        glossaryUpdateRequest.setDescription("test update");
        assertThrows("A glossary with the same name is not allowed at the same level  name:" + newName, RuntimeException.class, () -> glossaryService.update(glossarySon2.getId(), glossaryUpdateRequest2));
//
//      move

        Glossary glossarySon1 = glossaries.get("glossarySon1");
        Glossary glossaryChild = glossaries.get("glossaryChild");
        String glossarySon1NewName = "glossaryChild";//exist
        GlossaryRequest glossaryUpdateRequest3 = createGlossaryRequestWithParent(glossaryChild.getParentId(), assetList, glossarySon1NewName);
        glossaryUpdateRequest.setDescription("test update");
        assertThrows("A glossary with the same name is not allowed at the same level  name:" + newName, RuntimeException.class, () -> glossaryService.update(glossarySon1.getId(), glossaryUpdateRequest3));


    }

    @Test
    public void test_fetch_glossary_manager_operation() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Long glossary1_1Id = glossary1_1.getId();
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        GlossaryRole role = GlossaryRole.GLOSSARY_MANAGER;
        SecurityInfo securityInfo = glossaryService.fetchGlossaryOperation(glossary1_1Id);
        assertThat(securityInfo, notNullValue());
        assertThat(securityInfo.getSecurityModule(), is(securityModule));
        assertThat(securityInfo.getKunRole(), is(role));
        assertThat(securityInfo.getSourceSystemId(), is(glossary1_1Id));
        assertThat(securityInfo.getOperations(), is(role.getUserOperation()));
    }

    @Test
    public void test_fetch_operation() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        Long glossary1_1Id = glossary1_1.getId();
        GlossaryRole glossaryViewer = GlossaryRole.GLOSSARY_VIEWER;
        mockRole(glossaryViewer);
        SecurityInfo securityInfo = glossaryService.fetchGlossaryOperation(glossary1_1Id);
        assertThat(securityInfo, notNullValue());
        assertThat(securityInfo.getSecurityModule(), is(SecurityModule.GLOSSARY));
        assertThat(securityInfo.getKunRole(), is(glossaryViewer));
        assertThat(securityInfo.getSourceSystemId(), is(glossary1_1Id));
        assertThat(securityInfo.getOperations(), is(glossaryViewer.getUserOperation()));
    }

    @Test
    public void test_fetch_Inherit_permissions_operation() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        mockRole(GlossaryRole.GLOSSARY_EDITOR);
        Glossary glossary1_1 = glossaryTree.get("glossary1_1");
        GlossaryRole glossaryEditor = GlossaryRole.GLOSSARY_EDITOR;
        Long glossary1_1Id = glossary1_1.getId();
        ScopeRole scopeRole1_1 = ScopeRole.newBuilder().setRolename(glossaryEditor.getName()).setSourceSystemId(glossary1_1Id.toString()).build();
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        Long glossary3_1Id = glossary3_1.getId();
        GlossaryRole glossaryViewer = GlossaryRole.GLOSSARY_VIEWER;
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        ScopeRole scopeRole3_1 = ScopeRole.newBuilder().setRolename(glossaryViewer.getName()).setSourceSystemId(glossary3_1Id.toString()).build();
        RoleOnSpecifiedResourcesResp resourcesResp = MockSecurityRpcClientFactory.mockUserRoleRespGlossary("test", ImmutableList.of(scopeRole1_1, scopeRole3_1));
        Mockito.when(securityRpcClient.findRoleOnSpecifiedResources(Mockito.eq(securityModule.name()), anyList())).thenReturn(resourcesResp);
        SecurityInfo securityInfo = glossaryService.fetchGlossaryOperation(glossary3_1Id);
        assertThat(securityInfo, notNullValue());
        assertThat(securityInfo.getSecurityModule(), is(securityModule));
        assertThat(securityInfo.getKunRole().getName(), not(scopeRole3_1.getRolename()));
        assertThat(securityInfo.getKunRole().getName(), is(scopeRole1_1.getRolename()));
        assertThat(securityInfo.getSourceSystemId(), is(glossary3_1Id));
        assertThat(securityInfo.getOperations(), is(glossaryEditor.getUserOperation()));
    }

    @Test
    public void test_fetch_copy_operation() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        Long glossary3_2Id = glossary3_2.getId();
        Long glossary3_1Id = glossary3_1.getId();
        GlossaryRole glossaryEditor = GlossaryRole.GLOSSARY_EDITOR;
        GlossaryRole glossaryViewer = GlossaryRole.GLOSSARY_VIEWER;
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        ScopeRole scopeRole3_1 = ScopeRole.newBuilder().setRolename(glossaryViewer.getName()).setSourceSystemId(glossary3_1Id.toString()).build();
        mockRole(glossaryEditor);
        RoleOnSpecifiedResourcesResp resourcesResp = MockSecurityRpcClientFactory.mockUserRoleRespGlossary("test", ImmutableList.of(scopeRole3_1));
        Mockito.when(securityRpcClient.findRoleOnSpecifiedResources(Mockito.eq(securityModule.name()), anyList())).thenReturn(resourcesResp);
        SecurityInfo securityInfo = glossaryService.fetchGlossaryOperation(glossary3_1Id);
        assertThat(securityInfo, notNullValue());
        assertThat(securityInfo.getSecurityModule(), is(securityModule));
        assertThat(securityInfo.getKunRole().getName(), is(scopeRole3_1.getRolename()));
        assertThat(securityInfo.getSourceSystemId(), is(glossary3_1Id));
        assertThat(securityInfo.getOperations(), not(glossaryViewer.getUserOperation()));
        assertThat(securityInfo.getOperations().size(), is(glossaryViewer.getUserOperation().size() + 1));
        assertTrue(securityInfo.getOperations().contains(GlossaryUserOperation.COPY_GLOSSARY));
    }

    @Test
    public void test_fetch_function_operation() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Long glossary3_2Id = glossary3_2.getId();
        GlossaryRole glossaryEditor = GlossaryRole.GLOSSARY_EDITOR;
        ScopeRole scopeRole3_2 = ScopeRole.newBuilder().setRolename(glossaryEditor.getName()).setSourceSystemId(glossary3_2Id.toString()).build();

        Glossary glossary3_1 = glossaryTree.get("glossary3_1");
        Long glossary3_1Id = glossary3_1.getId();
        GlossaryRole glossaryViewer = GlossaryRole.GLOSSARY_VIEWER;
        SecurityModule securityModule = SecurityModule.GLOSSARY;
        ScopeRole scopeRole3_1 = ScopeRole.newBuilder().setRolename(glossaryViewer.getName()).setSourceSystemId(glossary3_1Id.toString()).build();

        mockRole(glossaryEditor);
        RoleOnSpecifiedResourcesResp resourcesResp1 = MockSecurityRpcClientFactory.mockUserRoleRespGlossary("test", ImmutableList.of(scopeRole3_1));
        Mockito.when(securityRpcClient.findRoleOnSpecifiedResources(Mockito.eq(securityModule.name()), Mockito.argThat(argument -> argument.contains(glossary3_1Id.toString())))).thenReturn(resourcesResp1);
        RoleOnSpecifiedResourcesResp resourcesResp2 = MockSecurityRpcClientFactory.mockUserRoleRespGlossary("test", ImmutableList.of(scopeRole3_2));
        Mockito.when(securityRpcClient.findRoleOnSpecifiedResources(Mockito.eq(securityModule.name()), Mockito.argThat(argument -> argument.contains(glossary3_2Id.toString())))).thenReturn(resourcesResp2);
        GlossaryCopyRequest copyRequest = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary3_2.getParentId(), glossary3_1.getId(), CopyOperation.CONTAINS_CHILDREN);
        assertThrows(PermissionDeniedDataAccessException.class, () -> glossaryService.copy(copyRequest));
        GlossaryCopyRequest copyRequest1 = MockGlossaryBasicFactory.createGlossaryCopyRequest(glossary3_2.getId(), glossary3_1.getId(), CopyOperation.CONTAINS_CHILDREN);
        assertThat(glossaryService.copy(copyRequest1), notNullValue());
    }

    @Test
    public void test_add_owner_checkAuth() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Long id = glossaryService.addOwner(glossary3_2.getId(), "test1", true);
        assertThat(id, is(glossary3_2.getId()));
    }

    @Test
    public void test_add_owner() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        GlossaryRole glossaryEditor = GlossaryRole.GLOSSARY_EDITOR;
        mockRole(glossaryEditor);
        Long id = glossaryService.addOwner(glossary3_2.getId(), "test1", false);
        assertThat(id, is(glossary3_2.getId()));
    }

    @Test
    public void test_remove_owner_checkAuth() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        Long id = glossaryService.removeOwner(glossary3_2.getId(), "test1", true);
        assertThat(id, is(glossary3_2.getId()));
    }

    @Test
    public void test_remove_owner() {
        Map<String, Glossary> glossaryTree = mockTreeMap("glossary1_1", "glossary2_1", "glossary3_1", "glossary3_2");
        Glossary glossary3_2 = glossaryTree.get("glossary3_2");
        GlossaryRole glossaryEditor = GlossaryRole.GLOSSARY_EDITOR;
        mockRole(glossaryEditor);
        Long id = glossaryService.removeOwner(glossary3_2.getId(), "test1", false);
        assertThat(id, is(glossary3_2.getId()));
    }

    @Test
    public void test_add_glossary_resource() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList, "glossary");
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Set<Long> all = new HashSet<>(assetList);
        List<Long> addList = ImmutableList.of(2L, 3L, 4L);
        all.addAll(addList);

        assertTrue(glossaryService.addGlossaryResource(glossary.getId(), addList));
        Glossary afterGlossary = glossaryService.fetchGlossary(glossary.getId());
        assertTrue(afterGlossary.getAssets().stream().allMatch(asset -> all.contains(asset.getId())));
    }

    @Test
    public void test_remove_glossary_resource() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList, "glossary");
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Set<Long> all = new HashSet<>(assetList);
        List<Long> removeList = ImmutableList.of(2L, 3L);
        all.removeAll(removeList);

        assertTrue(glossaryService.removeGlossaryResource(glossary.getId(), removeList));
        mockDatasetBasicInfoList(Lists.newArrayList(all));
        Glossary afterGlossary = glossaryService.fetchGlossary(glossary.getId());
        assertTrue(afterGlossary.getAssets().stream().allMatch(asset -> all.contains(asset.getId())));
    }

    public void mockRole(GlossaryRole role) {
        RoleOnSpecifiedModuleResp glossaryManagerRole = MockSecurityRpcClientFactory.mockRoleOnSpecifiedModule("test", role);
        Mockito.when(securityRpcClient.findRoleOnSpecifiedModule(SecurityModule.GLOSSARY.name())).thenReturn(glossaryManagerRole);
    }

}