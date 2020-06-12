package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.persistence.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: JieChen
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
