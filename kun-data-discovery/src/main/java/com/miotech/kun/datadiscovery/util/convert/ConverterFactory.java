package com.miotech.kun.datadiscovery.util.convert;


/**
 * @program: kun
 * @description: a facoty converters
 * @author: zemin  huang
 * @create: 2022-02-09 09:43
 **/
public interface ConverterFactory<S, R> {

    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}

