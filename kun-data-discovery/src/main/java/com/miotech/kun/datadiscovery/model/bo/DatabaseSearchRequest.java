package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class DatabaseSearchRequest extends PageInfo {

    private String search;
}
