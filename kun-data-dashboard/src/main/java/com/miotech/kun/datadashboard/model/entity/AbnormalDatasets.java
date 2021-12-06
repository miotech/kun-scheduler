package com.miotech.kun.datadashboard.model.entity;

import com.google.common.collect.Lists;
import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AbnormalDatasets extends PageInfo {

    private List<AbnormalDataset> abnormalDatasets = Lists.newArrayList();

    public void add(AbnormalDataset abnormalDataset) {
        abnormalDatasets.add(abnormalDataset);
    }

}
