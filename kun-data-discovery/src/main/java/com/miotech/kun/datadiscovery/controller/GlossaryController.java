package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.datadiscovery.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscovery.model.entity.GlossaryId;
import com.miotech.kun.datadiscovery.model.entity.GlossaryPage;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Melo
 * @created: 5/29/20
 */

@RestController
@RequestMapping("/kun/api/v1")
public class GlossaryController {

    @Autowired
    GlossaryService glossaryService;

    @PostMapping("/metadata/glossary/add")
    public RequestResult<Glossary> add(@RequestBody GlossaryRequest request) {
        return RequestResult.success(glossaryService.add(request));
    }

    @GetMapping("/metadata/glossary/children")
    public RequestResult<GlossaryChildren> getChildren(@RequestParam(value = "parentId", required = false) Long parentId) {
        return RequestResult.success(glossaryService.getChildren(parentId));
    }

    @GetMapping("/metadata/glossary/{id}/detail")
    public RequestResult<Glossary> getDetail(@PathVariable("id") Long id) {
        return RequestResult.success(glossaryService.getDetail(id));
    }

    @PostMapping("/metadata/glossary/{id}/update")
    public RequestResult<Glossary> update(@PathVariable("id") Long id,
                                          @RequestBody GlossaryRequest glossaryRequest) {
        return RequestResult.success(glossaryService.update(id, glossaryRequest));
    }

    @DeleteMapping("/metadata/glossary/{id}")
    public RequestResult<GlossaryId> delete(@PathVariable("id") Long id) {
        GlossaryId glossaryId = new GlossaryId();
        glossaryId.setId(id);
        glossaryId.setParentId(glossaryService.getParentId(id));
        glossaryService.delete(id);
        return RequestResult.success(glossaryId);
    }

    @GetMapping("/metadata/glossaries/search")
    public RequestResult<GlossaryPage> search(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(glossaryService.search(basicSearchRequest));
    }
}
