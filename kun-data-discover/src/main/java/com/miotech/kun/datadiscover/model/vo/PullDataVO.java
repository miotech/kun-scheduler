package com.miotech.kun.datadiscover.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class PullDataVO {
    private int table_count;
    private String duration;
}
