package com.miotech.kun.datadiscover.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscover.model.vo.TagListVO;
import com.miotech.kun.datadiscover.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@RestController
@RequestMapping("/kun/api/v1")
public class ReferenceController {

    @Autowired
    TagService tagService;

    @GetMapping("/metadata/tags/search")
    public RequestResult<TagListVO> getTags(@RequestParam("keyword") String keyword) {
        TagListVO vo = new TagListVO();
        vo.setTags(tagService.search(keyword));
        return RequestResult.success(vo);
    }

}
