package com.miotech.kun.datadiscovery.model.entity.rdm;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:07
 **/
@Data
@AllArgsConstructor
public class RefBaseTable {
    private RefTableMetaData refTableMetaData;
    private RefData refData;
}
