package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/10/24
 */
@Data
public class DatasetStats {

    Long rowCount;

    Watermark highWatermark;
}
