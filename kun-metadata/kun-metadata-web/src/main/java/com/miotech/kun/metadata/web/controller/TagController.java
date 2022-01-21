package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.TagService;

import java.util.List;

@Singleton
public class TagController {

    @Inject
    private TagService tagService;

    @RouteMapping(url = "/tags", method = "GET")
    public List<String> searchTags(@QueryParameter String keyword) {
        return tagService.searchTags(keyword);
    }

}
