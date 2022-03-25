package com.miotech.kun.datadiscovery.testing;

import com.google.common.collect.Sets;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class GlossaryServiceTest extends DataDiscoveryTestBase {

    @MockBean
    WorkflowClient workflowClient;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    GlossaryService glossaryService;

    @MockBean
    private DeployedTaskFacade deployedTaskFacade;


    @Test
    void test_getParentIdIsNull() {
        Long parentId = null;
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Long reParentId = glossaryService.getParentId(glossary.getId());
        assertThat(reParentId, is(parentId));
    }

    @Test
    void test_getParentIdNotNull() {
        Long parentId = null;
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        Long reChildParentId = glossaryService.getParentId(glossaryChild.getId());
        assertThat(reChildParentId, is(glossary.getId()));

    }

    private GlossaryRequest createGlossaryRequestWithParent(Long parentId, ImmutableList<Long> assetList) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setParentId(parentId);
        glossaryRequest.setName("glossary");
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(null, assetList);
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(null, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        glossaryService.createGlossary(glossaryRequest);

        GlossaryRequest glossary2Request = createGlossaryRequestWithParent(null, assetList);
        mockDatasetBasicInfoList(glossary2Request.getAssetIds());
        glossaryService.createGlossary(glossary2Request);

        GlossaryRequest glossary3Request = createGlossaryRequestWithParent(null, assetList);
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
        List<DatasetBasicInfo> collect = assetIds.stream()
                .map(id -> getDatasetBasicInfo(id, "testName" + id))
                .collect(Collectors.toList());
        ResponseEntity<List<DatasetBasicInfo>> responseEntity = new ResponseEntity(collect, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<DatasetBasicInfo>>) any())).thenReturn(responseEntity);
    }


    private DatasetBasicInfo getDatasetBasicInfo(long gid, String name) {
        DatasetBasicInfo datasetBasicInfo1 = new DatasetBasicInfo();
        datasetBasicInfo1.setGid(gid);
        datasetBasicInfo1.setType("dataset");
        datasetBasicInfo1.setName(name);
        datasetBasicInfo1.setDatasource("Hive");
        datasetBasicInfo1.setDatabase("dm");
        datasetBasicInfo1.setOwners(ImmutableList.of("test1", "test2"));
        return datasetBasicInfo1;
    }

    @Test
    void test_copy_parentIsNull() {
        Long parentId = null;
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);

        Glossary glossaryCopy = glossaryService.copy(glossary.getId());
        assertThat(glossaryCopy.getAssets(), is(glossary.getAssets()));
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossary.getName())));
        assertThat(glossaryCopy.getId(), not(glossary.getId()));
        assertThat(glossaryCopy.getParent(), is(glossary.getParent()));
        assertThat(glossaryCopy.getDescription(), is(glossary.getDescription()));
        assertThat(glossaryCopy.getCreateTime(), not(glossary.getCreateTime()));
        assertThat(glossaryCopy.getUpdateTime(), not(glossary.getUpdateTime()));

    }

    /**
     * null->glossary
     */
    @Test
    void test_update_basic_info() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
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
    void test_update_throw_Exption() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon);

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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon);

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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon);
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon3 = glossaryService.createGlossary(glossaryRequestSon);
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
    void test_copy() {
        /**
         *null->glossary->glossaryChild--->glossarySon1
         *                           ｜
         *                           ｜---> glossarySon2
         */

        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);

        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);

        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);

        Glossary glossaryCopy = glossaryService.copy(glossaryChild.getId());
//        test self info
        assertThat(glossaryCopy.getAssets(), is(glossaryChild.getAssets()));
        assertThat(glossaryCopy.getName(), is(GlossaryService.getCopyName(glossaryChild.getName())));
        assertThat(glossaryCopy.getId(), not(glossaryChild.getId()));
        assertThat(glossaryCopy.getParent(), is(glossaryChild.getParent()));
        assertThat(glossaryCopy.getDescription(), is(glossaryChild.getDescription()));
        assertThat(glossaryCopy.getCreateTime(), not(glossaryChild.getCreateTime()));
        assertThat(glossaryCopy.getUpdateTime(), not(glossaryChild.getUpdateTime()));

//        TEST DEEP COPY
        List<GlossaryBasicInfo> glossaryBasicChildInfoList = glossaryService.fetchChildrenBasicList(glossaryChild.getId());
        List<GlossaryBasicInfo> glossaryCopyChildBasicInfoList = glossaryService.fetchChildrenBasicList(glossaryCopy.getId());
        assertThat(glossaryBasicChildInfoList.size(), is(glossaryCopyChildBasicInfoList.size()));
        Glossary son1 = glossaryService.fetchGlossary(glossaryBasicChildInfoList.get(0).getId());
        Glossary copySon1 = glossaryService.fetchGlossary(glossaryCopyChildBasicInfoList.get(0).getId());
        assertThat(copySon1.getId(), is(not(son1.getId())));
        assertThat(copySon1.getName(), is(GlossaryService.getCopyName(son1.getName())));
        assertThat(copySon1.getDescription(), is(son1.getDescription()));
        assertThat(copySon1.getAssets().size(), is(son1.getAssets().size()));
        assertThat(copySon1.getAncestryGlossaryList().size(), is(son1.getAncestryGlossaryList().size()));
        assertThat(copySon1.getParentId(), is(not(son1.getParentId())));


    }

    @Test
    public void test_fetchChildrenBasicList_parent_null() {
        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
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
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList);
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList);
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon);

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
        GlossaryRequest glossaryRequest1 = createGlossaryRequestWithParent(parentId, assetList1);
        mockDatasetBasicInfoList(glossaryRequest1.getAssetIds());
        Glossary glossary1 = glossaryService.createGlossary(glossaryRequest1);

        ImmutableList<Long> assetList2 = ImmutableList.of(4L, 5L, 6L);
        GlossaryRequest glossaryRequest2 = createGlossaryRequestWithParent(glossary1.getId(), assetList2);
        mockDatasetBasicInfoList(glossaryRequest2.getAssetIds());
        Glossary glossary2 = glossaryService.createGlossary(glossaryRequest2);

        ImmutableList<Long> assetList3 = ImmutableList.of(7L, 8L);
        GlossaryRequest glossaryRequest3 = createGlossaryRequestWithParent(glossary2.getId(), assetList3);
        mockDatasetBasicInfoList(glossaryRequest3.getAssetIds());
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequest3);

        ImmutableList<Long> assetList4 = ImmutableList.of(1L, 5L, 8L);
        GlossaryRequest glossaryRequest4 = createGlossaryRequestWithParent(glossary2.getId(), assetList4);
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


    @Test
    void test_search() {
        /**
         *null->glossary->glossaryChild--->glossarySon1
         *                           ｜
         *                           ｜---> glossarySon2
         */

        ImmutableList<Long> assetList = ImmutableList.of(1L, 2L, 3L);
        List<Glossary> glossaries = Lists.newArrayList();
        Long parentId = null;
        GlossaryRequest glossaryRequest = createGlossaryRequestWithParent(parentId, assetList, "glossary");
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.createGlossary(glossaryRequest);
        glossaries.add(glossary);
        GlossaryRequest glossaryRequestChild = createGlossaryRequestWithParent(glossary.getId(), assetList, "glossaryChild");
        Glossary glossaryChild = glossaryService.createGlossary(glossaryRequestChild);
        glossaries.add(glossaryChild);
        GlossaryRequest glossaryRequestSon = createGlossaryRequestWithParent(glossaryChild.getId(), assetList, "glossarySon1");
        Glossary glossarySon1 = glossaryService.createGlossary(glossaryRequestSon);
        glossaries.add(glossarySon1);
        GlossaryRequest glossaryRequestSon1 = createGlossaryRequestWithParent(glossaryChild.getId(), assetList, "glossarySon2");
        Glossary glossarySon2 = glossaryService.createGlossary(glossaryRequestSon1);
        glossaries.add(glossarySon2);

        String keyword1 = "ch";
        GlossaryBasicSearchRequest basicSearchRequest1 = new GlossaryBasicSearchRequest();
        basicSearchRequest1.setKeyword(keyword1);
        mockSearch(glossaries, keyword1);
        SearchPage searchPage1 = glossaryService.search(basicSearchRequest1);
        List<SearchedInfo> searchedInfoList = searchPage1.getSearchedInfoList();
        assertThat(searchedInfoList.size(), is(1));
        assertThat(searchedInfoList.get(0).getGid(), is(glossaryChild.getId()));

        String keyword2 = "glossary";
        GlossaryBasicSearchRequest basicSearchRequest2 = new GlossaryBasicSearchRequest();
        basicSearchRequest2.setKeyword(keyword2);
        mockSearch(glossaries, keyword2);
        ArrayList<Long> longs2 = Lists.newArrayList(glossary.getId(), glossarySon1.getId());
        basicSearchRequest2.setGlossaryIds(longs2);
        SearchPage searchPage2 = glossaryService.search(basicSearchRequest2);
        String s = JSONUtils.toJsonString(RequestResult.success(searchPage2));
        System.out.println("json:"+s);
        List<SearchedInfo> searchedInfoList2 = searchPage2.getSearchedInfoList();
        assertThat(searchedInfoList2.size(), is(longs2.size()));
        assertTrue(searchedInfoList2.stream().map(SearchedInfo::getGid).anyMatch(longs2::contains));

//        不会包含当前节点和当前节点下的子节点
        String keyword3 = "Child";
        GlossaryBasicSearchRequest basicSearchRequest3 = new GlossaryBasicSearchRequest();
        basicSearchRequest3.setKeyword(keyword3);
        mockSearch(glossaries, keyword3);
        basicSearchRequest3.setCurrentId(glossaryChild.getId());
        SearchPage searchPage3 = glossaryService.search(basicSearchRequest3);
        List<SearchedInfo> searchedInfoList3 = searchPage3.getSearchedInfoList();
        assertThat(searchedInfoList3.size(), is(0));
        String keyword4 = "glossa";
        GlossaryBasicSearchRequest basicSearchRequest4 = new GlossaryBasicSearchRequest();
        basicSearchRequest4.setKeyword(keyword4);
        mockSearch(glossaries, keyword4);
        basicSearchRequest4.setCurrentId(glossaryChild.getId());
        SearchPage searchPage4 = glossaryService.search(basicSearchRequest4);
        List<SearchedInfo> searchedInfoList4 = searchPage4.getSearchedInfoList();
        assertThat(searchedInfoList4.size(), is(1));
        assertThat(searchedInfoList4.get(0).getGid(), is(glossary.getId()));


    }

    private void mockSearch(List<Glossary> glossaryList, String keyword1) {
        String upperCase = keyword1.toUpperCase();
        List<SearchedInfo> collect = glossaryList.stream().map(glossary -> SearchedInfo.Builder.newBuilder()
                        .withGid(glossary.getId())
                        .withResourceType(ResourceType.GLOSSARY)
                        .withName(glossary.getName())
                        .withDescription(glossary.getDescription())
                        .withResourceAttribute(new GlossaryResourceAttribute(glossary.getUpdateUser()))
                        .build()).filter(searchedInfo -> searchedInfo.getName().toUpperCase().contains(upperCase)
                        || searchedInfo.getDescription().toUpperCase().contains(upperCase))
                .collect(Collectors.toList());
        UniversalSearchInfo universalSearchInfo = new UniversalSearchInfo();
        universalSearchInfo.setSearchedInfoList(collect);
        ResponseEntity<UniversalSearchInfo> responseEntity = new ResponseEntity(universalSearchInfo, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), (Class<UniversalSearchInfo>) any())).thenReturn(responseEntity);
    }

}