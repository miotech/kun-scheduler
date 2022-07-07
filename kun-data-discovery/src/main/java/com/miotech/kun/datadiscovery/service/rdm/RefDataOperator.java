package com.miotech.kun.datadiscovery.service.rdm;

import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-21 14:24
 **/

public interface RefDataOperator {
    void override(RefTableVersionInfo refTableVersionInfo);

    void deactivate(String tableName);
}
