package com.miotech.kun.datadiscovery.controller;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.datadiscovery.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscovery.model.entity.GlossaryId;
import com.miotech.kun.datadiscovery.model.entity.SearchResult;
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

    @PostMapping("/metadata/glossary/{id}/graph/update")
    public RequestResult<IdVO> graphUpdate(@PathVariable("id") Long id,
                                           @RequestBody GlossaryGraphRequest glossaryGraphRequest) {
        IdVO vo = new IdVO();
        vo.setId(glossaryService.updateGraph(id, glossaryGraphRequest));
        return RequestResult.success(vo);
    }

    @PostMapping("/metadata/glossary/add")
    public RequestResult<Glossary> add(@RequestBody GlossaryRequest request) {
        return RequestResult.success(glossaryService.createGlossary(request));
    }

    @GetMapping("/metadata/glossary/children")
    public RequestResult<GlossaryChildren> getChildren(@RequestParam(value = "parentId", required = false) Long parentId) {
        return RequestResult.success(glossaryService.fetchGlossaryChildren(parentId));
    }

    @GetMapping("/metadata/glossary/{id}/detail")
    public RequestResult<Glossary> getDetail(@PathVariable("id") Long id) {
        return RequestResult.success(glossaryService.fetchGlossary(id));
    }

    @PostMapping("/metadata/glossary/{id}/update")
    public RequestResult<Glossary> update(@PathVariable("id") Long id,
                                          @RequestBody GlossaryRequest glossaryRequest) {
        Preconditions.checkNotNull(id, "Invalid argument `id`: null");
        Preconditions.checkNotNull(glossaryRequest, "Invalid argument `glossaryRequest`: null");
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
    public RequestResult<SearchResult> search(GlossaryBasicSearchRequest basicSearchRequest) {
        return RequestResult.success(glossaryService.search(basicSearchRequest));
    }

    @PostMapping("/metadata/glossary/copy/{id}")
    public RequestResult<Glossary> copy(@PathVariable("id") Long id) {
        return RequestResult.success(glossaryService.copy(id));
    }
}
