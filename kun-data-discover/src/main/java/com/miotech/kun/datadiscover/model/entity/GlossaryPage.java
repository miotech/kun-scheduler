package com.miotech.kun.datadiscover.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@Data
public class GlossaryPage {

    private List<GlossaryBasic> glossaries = new ArrayList<>();

    public void add(GlossaryBasic glossary) {
        glossaries.add(glossary);
    }
}
