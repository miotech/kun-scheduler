package com.miotech.kun.datadiscovery.service.rdm;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.glue.model.DeleteTableResult;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-21 14:24
 **/

public interface RefDataOperator {
    AmazonWebServiceResult overwrite(RefTableVersionInfo refTableVersionInfo);


    DeleteTableResult remove(String databaseName, String tableName);

}
