package com.miotech.kun.datadiscovery.testing;

import com.google.common.collect.Sets;
import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class GlossaryServiceTest extends KunAppTestBase {

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
        Assertions.assertTrue(assets.stream().map(Asset::getId).allMatch(assetIds::contains));
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

    @Test
    void test_copy() {
/**
 *null->glossary->glossaryChild--->glossarySon1
 *                           ｜
 *                           ｜---> glossarySon2
 */

//
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
    public void  test_fetchChildrenBasicList_parent_null(){
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
    public  void  test_fetchGlossary_AncestryGlossaryList(){
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
        assertThat(ancestryGlossaryList.size(),is(2));
        assertThat(ancestryGlossaryList.get(0).getId(),is(glossary.getId()));
        assertThat(ancestryGlossaryList.get(0).getName(),is(glossary.getName()));
        assertThat(ancestryGlossaryList.get(1).getId(),is(glossaryChild.getId()));
        assertThat(ancestryGlossaryList.get(1).getName(),is(glossaryChild.getName()));


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
        assertThat(glossaryBasic2.getDataSetCount(),is(depthDataset1.size()));

        GlossaryChildren glossary1Child = glossaryService.fetchGlossaryChildren(glossary1.getId());
        assertThat(glossary1Child.getParentId(), is(glossary1.getId()));
        List<GlossaryBasicInfoWithCount> glossary1BasicList = glossary1Child.getChildren();
        assertThat(glossary1BasicList.size(), is(1));
        GlossaryBasicInfoWithCount glossary1Basic1= glossary1BasicList.get(0);
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
        GlossaryBasicInfoWithCount glossary2Basic1= glossary2BasicList.get(0);
        assertThat(glossary2Basic1.getName(), is(glossarySon2.getName()));
        assertThat(glossary2Basic1.getChildrenCount(), is(0));
        assertThat(glossary2Basic1.getDataSetCount(), is(assetList4.size()));


    }

}