package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.service.rdm.RefFileResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 18:19
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefStorageCsvFileResolver implements RefFileResolver<RefData> {

    @Override
    public RefData resolve(InputStream is) throws IOException {
        return getRefDataByCSV(is);
    }
}
