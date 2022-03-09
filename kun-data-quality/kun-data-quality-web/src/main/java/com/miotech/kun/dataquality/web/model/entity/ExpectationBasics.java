package com.miotech.kun.dataquality.web.model.entity;

import com.google.common.collect.Lists;
import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class ExpectationBasics extends PageInfo {

    private List<ExpectationBasic> expectationBasics = Lists.newArrayList();

    public void add(ExpectationBasic expectationBasic) {
        expectationBasics.add(expectationBasic);
    }

}
