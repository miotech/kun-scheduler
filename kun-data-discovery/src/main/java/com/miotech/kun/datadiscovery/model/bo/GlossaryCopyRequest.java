package com.miotech.kun.datadiscovery.model.bo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: kun
 * @description: copy glossary
 * @author: zemin  huang
 * @create: 2022-05-09 14:54
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlossaryCopyRequest implements Serializable {
    private Long parentId;
    private Long sourceId;
    private CopyOperation copyOperation;
}
