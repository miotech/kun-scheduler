package com.miotech.kun.datadiscovery.service.rdm.file;


import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefInputSource;
import com.miotech.kun.datadiscovery.service.rdm.RefFileResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:59
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefUploadFileBuilder implements ApplicationContextAware {


    private ApplicationContext applicationContext;

    public RefBaseTable parse(RefInputSource is) throws IOException {
        UploadFileType uploadFileType = is.getRefUploadFileDescription().getUploadFileType();
        RefUploadFileResolver refFileResolver = applicationContext.getBean(uploadFileType.getResolver());
        return refFileResolver.resolve(is.getInputStream());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
