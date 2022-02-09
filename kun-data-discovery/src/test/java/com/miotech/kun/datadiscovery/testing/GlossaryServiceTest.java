package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.Asset;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasic;
import com.miotech.kun.datadiscovery.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

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


    @Test
    void test_getParentId() {
        Long parentId = null;
        GlossaryRequest glossaryRequest = getGlossaryRequest(parentId);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.add(glossaryRequest);
        Long reParentId = glossaryService.getParentId(glossary.getId());
        assertThat(reParentId, is(parentId));
        GlossaryRequest glossaryRequestChild = getGlossaryRequest(glossary.getId());
        Glossary glossaryChild = glossaryService.add(glossaryRequestChild);
        Long reChildParentId = glossaryService.getParentId(glossaryChild.getId());
        assertThat(reChildParentId, is(glossary.getId()));
    }

    private GlossaryRequest getGlossaryRequest(Long parentId) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setParentId(parentId);
        glossaryRequest.setName("childGlossary");
        glossaryRequest.setDescription("test  return  parent id node");
        glossaryRequest.setAssetIds(ImmutableList.of(1L, 2L, 3L));
        return glossaryRequest;
    }


    @Test
    void test_addGlossary() {
        GlossaryRequest glossaryRequest = getGlossaryRequest(null);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.add(glossaryRequest);

        assertThat(glossary, is(notNullValue()));
        assertThat(glossary.getId(), is(notNullValue()));
        assertThat(glossary.getName(), is(glossaryRequest.getName()));
        List<Long> assetIds = glossaryRequest.getAssetIds();
        List<Asset> assets = glossary.getAssets();
        assertThat(assets, is(notNullValue()));
        assertThat(assets.size(), is(glossary.getAssets().size()));
        Assertions.assertTrue(assets.stream().map(Asset::getId).allMatch(assetIds::contains));
    }

    private void mockDatasetBasicInfoList(List<Long> assetIds) {
        List<DatasetBasicInfo> collect = assetIds.stream()
                .map(id -> getDatasetBasicInfo(id, "testName" + id))
                .collect(Collectors.toList());
        ResponseEntity<List<DatasetBasicInfo>> responseEntity=new ResponseEntity(collect, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<DatasetBasicInfo>>) any())).thenReturn(responseEntity);
    }

    private DatasetBasicInfo getDatasetBasicInfo(long gid, String name) {
        DatasetBasicInfo datasetBasicInfo1 = new DatasetBasicInfo();
        datasetBasicInfo1.setGid(gid);
        datasetBasicInfo1.setType("dataset");
        datasetBasicInfo1.setName(name);
        datasetBasicInfo1.setDatasource("Hive");
        datasetBasicInfo1.setDatabase("dm");
        datasetBasicInfo1.setOwners(ImmutableList.of("test1","test2"));
        return datasetBasicInfo1;
    }


    @Test
    void test_copy() {
        Long parentId = null;
        GlossaryRequest glossaryRequest = getGlossaryRequest(parentId);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.add(glossaryRequest);

        GlossaryRequest glossaryRequestChild = getGlossaryRequest(glossary.getId());
        Glossary glossaryChild = glossaryService.add(glossaryRequestChild);

        GlossaryRequest glossaryRequestSon = getGlossaryRequest(glossaryChild.getId());
        glossaryService.add(glossaryRequestSon);
        glossaryService.add(glossaryRequestSon);

        Glossary glossaryCopy = glossaryService.copy(glossaryChild.getId());

        assertThat(glossaryCopy.getAssets(),is(glossaryChild.getAssets()));
        assertThat(glossaryCopy.getName(),is(glossaryChild.getName()));
        assertThat(glossaryCopy.getId(),not(glossaryChild.getId()));
        assertThat(glossaryCopy.getParent(),is(glossaryChild.getParent()));
        assertThat(glossaryCopy.getDescription(),is(glossaryChild.getDescription()));
        assertThat(glossaryCopy.getCreateTime(),not(glossaryChild.getCreateTime()));
        assertThat(glossaryCopy.getUpdateTime(),not(glossaryChild.getUpdateTime()));

    }

    @Test
    public void  test_getGlossaryChild(){
//        load data
        Long parentId = null;
        GlossaryRequest glossaryRequest = getGlossaryRequest(parentId);
        mockDatasetBasicInfoList(glossaryRequest.getAssetIds());
        Glossary glossary = glossaryService.add(glossaryRequest);
        GlossaryRequest glossaryRequestChild = getGlossaryRequest(glossary.getId());
        Glossary glossaryChild = glossaryService.add(glossaryRequestChild);

//        testing function
        GlossaryChildren children = glossaryService.getChildren(parentId);
        assertThat(children.getParentId(),is(parentId));
        List<GlossaryBasic> glossaryBasicList = children.getChildren();
        assertThat(glossaryBasicList.size(),is(1));
        GlossaryBasic glossaryBasic = glossaryBasicList.get(0);
        assertThat(glossaryBasic.getName(),is(glossaryChild.getName()));
        assertThat(glossaryBasic.getChildrenCount(),is(1L));
        assertThat(glossaryBasic.getDataSetCount(),is(3L));


    }

}