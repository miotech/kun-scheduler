package com.miotech.kun.datadiscovery.service.rdm.file;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public enum UploadFileType {
    CSV(ImmutableSet.of(".csv"), RefUploadCsvFileResolver.class),
    EXECL(ImmutableSet.of(".xlsx", ".xls"), RefUploadExeclFileResolver.class);


    private ImmutableSet<String> endWithSet;
    private Class<? extends RefUploadFileResolver> resolver;

    UploadFileType(ImmutableSet<String> endWithSet, Class<? extends RefUploadFileResolver> resolver) {
        this.endWithSet = endWithSet;
        this.resolver = resolver;
    }

    public Class<? extends RefUploadFileResolver> getResolver() {
        return resolver;
    }

    public static UploadFileType getFileType(String endWith) {
        UploadFileType[] values = UploadFileType.values();
        Optional<UploadFileType> first = Arrays.stream(values).filter(uploadFileType -> uploadFileType.getEndWithSet().contains(endWith)).findFirst();
        if (!first.isPresent()) {
            log.error("end with :{} is  not support", endWith);
            throw new IllegalArgumentException(String.format("file type :%s is  not support", endWith));
        }
        log.debug("fileType :{}", first);
        return first.get();
    }

    public ImmutableSet<String> getEndWithSet() {
        return endWithSet;
    }
}
