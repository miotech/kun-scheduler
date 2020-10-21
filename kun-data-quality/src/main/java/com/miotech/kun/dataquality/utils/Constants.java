package com.miotech.kun.dataquality.utils;

import com.alibaba.druid.util.JdbcConstants;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author: Melo
 * @created: 8/13/20
 */
public class Constants {

    final public static String DQ_RESULT_PREFIX = "DQ_RECORD>>>";
    final public static String DQ_RESULT_SUCCESS = "SUCCESS";
    final public static String DQ_RESULT_FAILED = "FAILED";

    final public static Map<String, String> DATASOURCE_TO_DRUID_TYPE = Maps.newHashMap();

    static {
        DATASOURCE_TO_DRUID_TYPE.put("AWS", JdbcConstants.HIVE);
        DATASOURCE_TO_DRUID_TYPE.put("PostgreSQL", JdbcConstants.POSTGRESQL);
        DATASOURCE_TO_DRUID_TYPE.put("Elasticsearch", JdbcConstants.ELASTIC_SEARCH);
    }
}
