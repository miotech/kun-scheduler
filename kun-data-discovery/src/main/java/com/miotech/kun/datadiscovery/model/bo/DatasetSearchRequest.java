package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class DatasetSearchRequest extends BasicSearchRequest {

    private String datasource;

    private String database;

    private String schema;

    private String type;

    private String tags;

    private String owners;
}
