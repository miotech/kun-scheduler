package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.persistence.GlossaryRepository;
import com.miotech.kun.datadiscovery.util.convert.Converter;
import com.miotech.kun.datadiscovery.util.convert.DatasetBasicInfoConvertFactory;
import com.miotech.kun.datadiscovery.util.convert.GlossaryBaseInfoConvertFactory;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
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


    public Long getParentId(Long id) {
        return glossaryRepository.getParentId(id);
    }

    public Long updateGraph(Long id, GlossaryGraphRequest glossaryGraphRequest) {
        return glossaryRepository.updateGraph(id, glossaryGraphRequest);
    }

    public List<GlossaryBasic> getGlossariesByDataset(Long datasetGid) {
        if (datasetGid == null) {
            return Lists.newArrayList();
        }

        return glossaryRepository.getGlossariesByDataset(datasetGid);
    }

    public Glossary add(GlossaryRequest glossaryRequest) {
        long timestamp = System.currentTimeMillis();
        glossaryRequest.setCreateUser(getCurrentUsername());
        glossaryRequest.setCreateTime(timestamp);
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(timestamp);
        Long id = glossaryRepository.insert(glossaryRequest);
        return getGlossary(id);
    }

    public GlossaryChildren getChildren(Long parentId) {
        return glossaryRepository.findChildren(parentId);
    }


    public Glossary getGlossary(Long id) {
        GlossaryBasicInfo glossaryBasicInfo = glossaryRepository.findGlossaryBaseInfo(id);
        if (Objects.isNull(glossaryBasicInfo)){
            throw  new RuntimeException("glossary does not exist");
        }
        GlossaryBaseInfoConvertFactory glossaryBaseInfoToGlossary = new GlossaryBaseInfoConvertFactory();
        Converter<GlossaryBasicInfo, Glossary> converter = glossaryBaseInfoToGlossary.getConverter(Glossary.class);
        Glossary glossary = converter.convert(glossaryBasicInfo);
        assert glossary != null;
        if (Objects.nonNull(glossaryBasicInfo.getParentId())&&glossaryBasicInfo.getParentId() != 0) {
            GlossaryBasicInfo parentGlossaryBasicInfo = glossaryRepository.findGlossaryBaseInfo(glossaryBasicInfo.getParentId());
            Glossary parentGlossary = converter.convert(parentGlossaryBasicInfo);
            glossary.setParent(parentGlossary);
        }
        List<Long> glossaryToDataSetIdList = glossaryRepository.findGlossaryToDataSetIdList(id);

        glossary.setAssets(findAssets(glossaryToDataSetIdList));
        return glossary;
    }


    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(System.currentTimeMillis());
        glossaryRepository.update(id, glossaryRequest);
        return getGlossary(id);
    }

    public void delete(Long id) {
        glossaryRepository.delete(id);
    }

    public GlossaryPage search(BasicSearchRequest searchRequest) {
        return glossaryRepository.search(searchRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public Glossary copy(Long copyId) {
        Glossary oldGlossary = getGlossary(copyId);
        Long newGlossaryId = copySelf(oldGlossary).getId();
        return getGlossary(newGlossaryId);
    }



    private Glossary copySelf(Glossary oldGlossary) {
        GlossaryRequest glossaryRequest = new GlossaryRequest();
        glossaryRequest.setName(oldGlossary.getName());
        glossaryRequest.setParentId(oldGlossary.getParent().getId());
        glossaryRequest.setDescription(oldGlossary.getDescription());
        List<Asset> assets = oldGlossary.getAssets();
        if (CollectionUtils.isNotEmpty(assets)) {
            List<Long> assetIds = assets.stream().map(Asset::getId).collect(Collectors.toList());
          glossaryRequest.setAssetIds(assetIds);
        }
        return add(glossaryRequest);
    }



    private  List<Asset> findAssets(List<Long> glossaryToDataSetIdList) {
        if (CollectionUtils.isEmpty(glossaryToDataSetIdList)) {
            return Lists.newArrayList();
        }
        String suggestColumnUrl = url + "/dataset/id_list";


        ParameterizedTypeReference<List<DatasetBasicInfo>> typeRef = new ParameterizedTypeReference<List<DatasetBasicInfo>>() {};
        ResponseEntity<List<DatasetBasicInfo>> responseEntity = restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(glossaryToDataSetIdList), typeRef);
        List<DatasetBasicInfo> datasetBasicInfos= responseEntity.getBody();
        Converter<DatasetBasicInfo, Asset> converter = new DatasetBasicInfoConvertFactory().getConverter(Asset.class);
        assert datasetBasicInfos != null;
        return datasetBasicInfos.stream()
                .map(converter::convert)
                .collect(Collectors.toList());
    }


}
