package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.CopyOperation;
import com.miotech.kun.datadiscovery.model.bo.GlossaryCopyRequest;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfoWithCount;

public class MockGlossaryBasicFactory {

    private MockGlossaryBasicFactory() {
    }

    public static GlossaryBasicInfoWithCount create() {
        GlossaryBasicInfoWithCount glossaryBasic = new GlossaryBasicInfoWithCount();
        glossaryBasic.setId(IdGenerator.getInstance().nextId());
        glossaryBasic.setName("test glossary");
        glossaryBasic.setDescription("desc");
        glossaryBasic.setChildrenCount(0);
        glossaryBasic.setPrevId(null);
        return glossaryBasic;
    }

    public static GlossaryCopyRequest createGlossaryCopyRequest(Long parentId, Long sourceId, CopyOperation copyOperation) {
        return new GlossaryCopyRequest(parentId, sourceId, copyOperation);
    }
}
