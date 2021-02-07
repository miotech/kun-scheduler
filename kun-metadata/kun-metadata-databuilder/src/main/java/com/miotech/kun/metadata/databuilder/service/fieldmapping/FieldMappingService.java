package com.miotech.kun.metadata.databuilder.service.fieldmapping;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.DatasetFieldMappingDao;
import com.miotech.kun.metadata.core.model.DatasetFieldMapping;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class FieldMappingService {

    private static final Logger logger = LoggerFactory.getLogger(FieldMappingService.class);
    private static Cache<String, List<DatasetFieldMapping>> localCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final DatasetFieldMappingDao fieldMappingDao;

    @Inject
    public FieldMappingService(DatasetFieldMappingDao fieldMappingDao) {
        this.fieldMappingDao = fieldMappingDao;
    }

    public DatasetFieldType.Type parse(String datasourceType, String rawType) {
        List<DatasetFieldMapping> fieldMappingsCache = localCache.getIfPresent(datasourceType);
        if (CollectionUtils.isEmpty(fieldMappingsCache)) {
            List<DatasetFieldMapping> fieldMappings = fieldMappingDao.fetchByDatasourceType(datasourceType);
            localCache.put(datasourceType, fieldMappings);
            
            return parse(fieldMappings, rawType);
        }
        
        return parse(fieldMappingsCache, rawType);
    }

    public void clear() {
        localCache.invalidateAll();
    }
    
    private DatasetFieldType.Type parse(List<DatasetFieldMapping> fieldMappings, String rawType) {
        List<DatasetFieldMapping> matched = fieldMappings.stream().filter(fieldMapping -> {
            String pattern = fieldMapping.getPattern();
            Matcher matcher = Pattern.compile(pattern).matcher(rawType);
            boolean b = matcher.find();
            return b;
        }).collect(Collectors.toList());

        if (matched.size() != 1) {
            logger.warn("For the rawType: {}, 0 or more pattern were found, return `UNKNOWN`", rawType);
            return DatasetFieldType.Type.UNKNOWN;
        }

        return DatasetFieldType.Type.valueOf(matched.get(0).getType());
    }

}
