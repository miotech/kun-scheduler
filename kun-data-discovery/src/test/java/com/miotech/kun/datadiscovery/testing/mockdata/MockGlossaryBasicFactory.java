package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasic;

public class MockGlossaryBasicFactory {

    private MockGlossaryBasicFactory() {
    }

    public static GlossaryBasic create() {
        GlossaryBasic glossaryBasic = new GlossaryBasic();
        glossaryBasic.setId(IdGenerator.getInstance().nextId());
        glossaryBasic.setName("test glossary");
        glossaryBasic.setDescription("desc");
        glossaryBasic.setChildrenCount(0L);
        glossaryBasic.setPrevId(null);
        return glossaryBasic;
    }

}
