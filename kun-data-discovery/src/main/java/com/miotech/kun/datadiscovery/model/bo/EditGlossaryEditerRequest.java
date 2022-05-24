package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-05-23 14:58
 **/
@Data
public class EditGlossaryEditerRequest implements Serializable {
    private String userName;
    private Long sourceSystemId;
}
