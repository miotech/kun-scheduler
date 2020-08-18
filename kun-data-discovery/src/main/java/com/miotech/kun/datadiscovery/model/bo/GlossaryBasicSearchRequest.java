package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/8/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryBasicSearchRequest extends BasicSearchRequest {

    List<Long> glossaryIds;
}
