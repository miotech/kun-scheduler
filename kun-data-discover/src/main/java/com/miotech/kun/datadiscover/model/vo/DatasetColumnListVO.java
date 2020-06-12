package com.miotech.kun.datadiscover.model.vo;

import com.miotech.kun.datadiscover.model.entity.DatasetColumn;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DatasetColumnListVO {
    private List<DatasetColumn> columns;
}
