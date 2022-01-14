package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasic;
import com.miotech.kun.datadiscovery.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscovery.model.entity.GlossaryPage;
import com.miotech.kun.datadiscovery.persistence.GlossaryRepository;
import com.miotech.kun.security.service.BaseSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Service
public class GlossaryService extends BaseSecurityService {

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
        return glossaryRepository.insert(glossaryRequest);
    }

    public GlossaryChildren getChildren(Long parentId) {
        return glossaryRepository.findChildren(parentId);
    }

    public Glossary getDetail(Long id) {
        return glossaryRepository.find(id);
    }

    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
        glossaryRequest.setUpdateUser(getCurrentUsername());
        glossaryRequest.setUpdateTime(System.currentTimeMillis());
        return glossaryRepository.update(id, glossaryRequest);
    }

    public void delete(Long id) {
        glossaryRepository.delete(id);
    }

    public GlossaryPage search(BasicSearchRequest searchRequest) {
        return glossaryRepository.search(searchRequest);
    }
}
