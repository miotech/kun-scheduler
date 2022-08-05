package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import lombok.Data;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 09:47
 **/
@Data
public class ValidRefDataRequest {
    private RefBaseTable refBaseTable;
}
