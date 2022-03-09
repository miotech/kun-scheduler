package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: kun
 * @description: move glossary search
 * @author: zemin  huang
 * @create: 2022-03-09 13:53
 **/
@EqualsAndHashCode(callSuper = false)
@Data
public class GlossaryMovedSearchRequest extends BasicSearchRequest {

    private Long currentId;
}
