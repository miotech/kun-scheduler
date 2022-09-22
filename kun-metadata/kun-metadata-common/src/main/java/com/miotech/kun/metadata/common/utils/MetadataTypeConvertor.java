package com.miotech.kun.metadata.common.utils;

import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-30 17:30
 **/
public class MetadataTypeConvertor {
    public static DatasourceType covertStoreTypeToSourceType(DataStoreType storeType) {
        DatasourceType sourceType;
        switch (storeType) {
            case POSTGRES_TABLE:
                sourceType = DatasourceType.POSTGRESQL;
                break;
            case MONGO_COLLECTION:
                sourceType = DatasourceType.MONGODB;
                break;
            case ELASTICSEARCH_INDEX:
                sourceType = DatasourceType.ELASTICSEARCH;
                break;
            case ARANGO_COLLECTION:
                sourceType = DatasourceType.ARANGO;
                break;
            case HIVE_TABLE:
                sourceType = DatasourceType.HIVE;
                break;
            default:
                throw new IllegalStateException("not support storeType :" + storeType);
        }
        return sourceType;
    }

}
