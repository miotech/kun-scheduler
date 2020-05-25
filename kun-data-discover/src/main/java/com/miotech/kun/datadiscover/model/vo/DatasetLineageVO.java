package com.miotech.kun.datadiscover.model.vo;

import com.miotech.kun.datadiscover.model.entity.Lineage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DatasetLineageVO {
    private List<Lineage> upstream;
    private List<Lineage> downstream;
}
