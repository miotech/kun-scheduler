package com.miotech.kun.datadiscovery.model.vo;

import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 18:12
 **/

public class RefDataVersionFillInfo extends RefTableVersionFillInfo {
    private RefBaseTable refBaseTable;

    public RefDataVersionFillInfo(RefTableVersionInfo refTableVersionInfo) {
        super(refTableVersionInfo);
    }

    public RefBaseTable getRefBaseTable() {
        return refBaseTable;
    }

    public void setRefBaseTable(RefBaseTable refBaseTable) {
        this.refBaseTable = refBaseTable;
    }
}
