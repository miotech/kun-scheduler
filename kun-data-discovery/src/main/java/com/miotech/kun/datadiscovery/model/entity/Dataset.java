package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Dataset extends DatasetBasic {

    private Long rowCount;

    private List<DataTask> flows;

    private List<GlossaryBasicInfo> glossaries;
}
