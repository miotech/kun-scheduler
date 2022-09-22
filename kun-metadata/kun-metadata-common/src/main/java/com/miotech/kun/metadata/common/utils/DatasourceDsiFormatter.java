package com.miotech.kun.metadata.common.utils;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-15 09:58
 **/
public class DatasourceDsiFormatter {
    private static final String HOST_STRING = "host";
    private static final String PORT_STRING = "port";
    private static final String ATHENA_URL_STRING = "athenaUrl";

    public static String getDsi(DatasourceType datasourceType, Map<String, Object> datasourceConfig) {
        Preconditions.checkNotNull(datasourceConfig);
        Preconditions.checkNotNull(datasourceType);
        String dsi = "";
        switch (datasourceType) {
            case MYSQL:
            case ARANGO:
            case MONGODB:
            case POSTGRESQL:
            case ELASTICSEARCH:
                dsi = basicDsiFormat(datasourceType, datasourceConfig);
                break;
            case HIVE:
                dsi = basicDsiFormat(datasourceType, datasourceConfig);
                if (StringUtils.isBlank(dsi)) {
                    dsi = athenaDsiFormat(datasourceType, datasourceConfig);
                }
                break;
        }
        if (StringUtils.isBlank(dsi)) {
            throw new IllegalArgumentException(String.format("datasource config info deficiency,type:%s,config:%s", datasourceType.name(), JSONUtils.toJsonString(datasourceConfig)));
        }
        return dsi;
    }

    private static String basicDsiFormat(DatasourceType datasourceType, Map<String, Object> datasourceConfig) {
        String typeString = datasourceType.name().toLowerCase(Locale.ROOT);
        if (datasourceConfig.containsKey(HOST_STRING) && datasourceConfig.containsKey(PORT_STRING)) {
            return String.format("%s://%s:%s", typeString, datasourceConfig.get(HOST_STRING), datasourceConfig.get(PORT_STRING));
        } else {
            return StringUtils.EMPTY;
        }

    }

    private static String athenaDsiFormat(DatasourceType datasourceType, Map<String, Object> datasourceConfig) {
        String typeString = datasourceType.name().toLowerCase(Locale.ROOT);
        if (datasourceConfig.containsKey(ATHENA_URL_STRING)) {
            return String.format("%s://%s", typeString, datasourceConfig.get(ATHENA_URL_STRING));
        } else {
            return StringUtils.EMPTY;
        }

    }

}
