package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/11/5
 */
@Data
public class DataQualityHistoryRecords {

    @JsonSerialize(using= ToStringSerializer.class)
    Long caseId;

    List<DataQualityHistory> historyList = new ArrayList<>();
}
