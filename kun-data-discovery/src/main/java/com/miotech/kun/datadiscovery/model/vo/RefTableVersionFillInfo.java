package com.miotech.kun.datadiscovery.model.vo;

import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 18:12
 **/


public class RefTableVersionFillInfo extends BaseRefTableVersionInfo {
    private List<GlossaryBasicInfo> glossaryList;
    private List<Pair<Long, String>> lineageDatasetList;

    public RefTableVersionFillInfo(RefTableVersionInfo refTableVersionInfo) {
        super(refTableVersionInfo);
    }

    public List<GlossaryBasicInfo> getGlossaryList() {
        return glossaryList;
    }

    public void setGlossaryList(List<GlossaryBasicInfo> glossaryList) {
        this.glossaryList = glossaryList;
    }

    public List<Pair<Long, String>> getLineageDatasetList() {
        return lineageDatasetList;
    }

    public void setLineageDatasetList(List<Pair<Long, String>> lineageDatasetList) {
        this.lineageDatasetList = lineageDatasetList;
    }
}
