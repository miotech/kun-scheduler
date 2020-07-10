package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.persistence.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class TagService {

    @Autowired
    TagRepository tagRepository;

    public List<String> search(String keyword) {
        return tagRepository.search(keyword);
    }
}
