package com.miotech.kun.datadiscovery.util.convert;

import com.google.common.base.Splitter;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasic;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Objects;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-02-09 09:56
 **/
@Slf4j
public class DatasetSearchInfoConvertFactory implements ConverterFactory<SearchedInfo, DatasetBasic> {

    @Override
    public <T extends DatasetBasic> Converter<SearchedInfo, T> getConverter(Class<T> targetType) {


        return source -> {
            DatasetBasic datasetBasic = new DatasetBasic();
            datasetBasic.setGid(source.getGid());
            datasetBasic.setName(source.getName());
            datasetBasic.setDescription(source.getDescription());
            datasetBasic.setDeleted(source.isDeleted());
            DataSetResourceAttribute resourceAttribute;
            ResourceAttribute sourceResourceAttribute = source.getResourceAttribute();
            if (Objects.isNull(sourceResourceAttribute)) {
                return (T) datasetBasic;
            }
            if (sourceResourceAttribute instanceof DataSetResourceAttribute) {
                resourceAttribute = (DataSetResourceAttribute) sourceResourceAttribute;
            } else {
                log.error("sourceResourceAttribute class:{} is not DataSetResourceAttribute", sourceResourceAttribute.getClass());
                throw new ClassCastException("resourceAttribute is not  DataSetResourceAttribute");
            }
            datasetBasic.setDatasource(resourceAttribute.getDatasource());
            datasetBasic.setDatabase(resourceAttribute.getDatabase());
            datasetBasic.setSchema(resourceAttribute.getSchema());
            datasetBasic.setType(resourceAttribute.getType());
            if (StringUtils.isNotBlank(resourceAttribute.getTags())) {
                datasetBasic.setTags(Splitter.on(",").splitToList(resourceAttribute.getTags()));
            }
            if (StringUtils.isNotBlank(resourceAttribute.getOwners())) {
                datasetBasic.setOwners(Splitter.on(",").splitToList(resourceAttribute.getOwners()));
            }
            return (T) datasetBasic;
        };
    }
}
