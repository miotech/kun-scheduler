package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.TagDao;

import java.util.List;

@Singleton
public class TagService {

    @Inject
    private TagDao tagDao;

    public List<String> searchTags(String keyword) {
        return tagDao.searchTags(keyword);
    }

}
