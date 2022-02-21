package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@Data
public class GlossaryPage {

    private List<GlossaryBasicInfo> glossaries = new ArrayList<>();

    public void add(GlossaryBasicInfo glossary) {
        glossaries.add(glossary);
    }
}
