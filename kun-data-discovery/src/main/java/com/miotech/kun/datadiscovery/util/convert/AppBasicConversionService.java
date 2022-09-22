package com.miotech.kun.datadiscovery.util.convert;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;

/**
 * @program: kun
 * @description: app basic convert service
 * @author: zemin  huang
 * @create: 2022-02-24 14:29
 **/
public class AppBasicConversionService extends GenericConversionService {

    @Nullable
    private static volatile AppBasicConversionService sharedInstance;

    public AppBasicConversionService() {
        addBasicConverters(this);
    }

    private void addBasicConverters(ConverterRegistry converterRegistry) {
        converterRegistry.addConverterFactory(new DatasetBasicInfoConvertFactory());
        converterRegistry.addConverterFactory(new GlossaryBaseInfoConvertFactory());
        converterRegistry.addConverterFactory(new DatasetSearchInfoConvertFactory());
        converterRegistry.addConverterFactory(new GlossarySearchConvertFactory());
        converterRegistry.addConverterFactory(new ConnectionInfoBasicInfoConvertFactory());
        converterRegistry.addConverterFactory(new ConnectionInfoConvertFactory());

    }


    public static ConversionService getSharedInstance() {
        AppBasicConversionService cs = sharedInstance;
        if (cs == null) {
            synchronized (AppBasicConversionService.class) {
                cs = sharedInstance;
                if (cs == null) {
                    cs = new AppBasicConversionService();
                    sharedInstance = cs;
                }
            }
        }
        return cs;
    }

}


