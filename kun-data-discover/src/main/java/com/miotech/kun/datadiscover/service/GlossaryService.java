package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscover.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Glossary;
import com.miotech.kun.datadiscover.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscover.model.entity.GlossaryPage;
import com.miotech.kun.datadiscover.persistence.GlossaryRepository;
import com.miotech.kun.security.service.BaseSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Glossary add(GlossaryRequest glossaryRequest) {
        long timestamp = System.currentTimeMillis();
        glossaryRequest.setCreateUser(getCurrentUser());
        glossaryRequest.setCreateTime(timestamp);
        glossaryRequest.setUpdateUser(getCurrentUser());
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
        glossaryRequest.setUpdateUser(getCurrentUser());
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
