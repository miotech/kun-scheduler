package com.miotech.kun.datadiscovery.util.convert;

import org.springframework.lang.Nullable;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-02-09 09:50
 **/
public interface Converter<S, T> {


    @Nullable
    T convert(S source);

}
