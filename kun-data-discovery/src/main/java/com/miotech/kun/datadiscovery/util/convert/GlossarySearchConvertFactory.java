package com.miotech.kun.datadiscovery.util.convert;

import com.miotech.kun.datadiscovery.model.entity.GlossarySearchedInfo;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @program: kun
 * @description: glossarbaseInfo Glossary
 * @author: zemin  huang
 * @create: 2022-02-09 09:45
 **/
public class GlossarySearchConvertFactory implements ConverterFactory<SearchedInfo, GlossarySearchedInfo> {
    @Override
    public <T extends GlossarySearchedInfo> Converter<SearchedInfo, T> getConverter(Class<T> targetType) {
        return source -> {
            GlossarySearchedInfo glossarySearchedInfo = new GlossarySearchedInfo();
            glossarySearchedInfo.setGid(source.getGid());
            glossarySearchedInfo.setName(source.getName());
            glossarySearchedInfo.setResourceType(source.getResourceType());
            glossarySearchedInfo.setDescription(source.getDescription());
            glossarySearchedInfo.setResourceAttribute(source.getResourceAttribute());
            glossarySearchedInfo.setDeleted(source.isDeleted());
            return (T) glossarySearchedInfo;
        };
    }
}
