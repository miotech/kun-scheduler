package com.miotech.kun.datadiscovery.util.convert;

import com.miotech.kun.datadiscovery.model.entity.Glossary;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @program: kun
 * @description: glossarbaseInfo Glossary
 * @author: zemin  huang
 * @create: 2022-02-09 09:45
 **/
public class GlossaryBaseInfoConvertFactory  implements ConverterFactory<GlossaryBasicInfo, Glossary> {
    @Override
    public <T extends Glossary> Converter<GlossaryBasicInfo, T> getConverter(Class<T> targetType) {
        return source -> {
            Glossary glossary = new Glossary();
            glossary.setId(source.getId());
            glossary.setName(source.getName());
            glossary.setParentId(source.getParentId());
            glossary.setPrevId(source.getPrevId());
            glossary.setDescription(source.getDescription());
            glossary.setCreateUser(source.getCreateUser());
            glossary.setCreateTime(source.getCreateTime());
            glossary.setUpdateTime(source.getUpdateTime());
            glossary.setUpdateUser(source.getUpdateUser());
            glossary.setDeleted(source.isDeleted());
            return (T) glossary;
        };
    }
}
