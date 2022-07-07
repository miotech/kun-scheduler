package com.miotech.kun.datadiscovery.model.entity.rdm;

import lombok.Data;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:34
 **/
@Data
public class ValidationMessage {
    private final Long lineIndex;
    private final Long columnIndex;
    private final Object data;
    private final String message;
}
