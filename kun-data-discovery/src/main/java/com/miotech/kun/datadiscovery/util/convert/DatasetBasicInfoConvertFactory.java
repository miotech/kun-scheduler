package com.miotech.kun.datadiscovery.util.convert;

import com.miotech.kun.datadiscovery.model.entity.Asset;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-02-09 09:56
 **/
public class DatasetBasicInfoConvertFactory implements ConverterFactory<DatasetDetail, Asset> {

    @Override
    public <T extends Asset> Converter<DatasetDetail, T> getConverter(Class<T> targetType) {
        return source -> {
            Asset asset = new Asset();
            asset.setId(source.getGid());
            asset.setType("dataset");
            asset.setName(source.getName());
            asset.setDatabase(source.getDatabase());
            asset.setDatasource(source.getDatasource());
            asset.setDescription(source.getDescription());
            asset.setOwner(source.getOwners());
            return (T) asset;
        };
    }
}
