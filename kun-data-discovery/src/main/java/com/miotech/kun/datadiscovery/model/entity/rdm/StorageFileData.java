package com.miotech.kun.datadiscovery.model.entity.rdm;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: temp_demo
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-28 14:54
 **/
@Data
@AllArgsConstructor
public class StorageFileData {
    private String dataPath;
    private final RefData refData;


}
