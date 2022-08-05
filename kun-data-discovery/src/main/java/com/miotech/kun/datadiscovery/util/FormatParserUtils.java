package com.miotech.kun.datadiscovery.util;

import org.apache.commons.csv.CSVFormat;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-30 17:02
 **/
public class FormatParserUtils {
    public static CSVFormat withHeaderCSVFormat() {
        return CSVFormat.DEFAULT.withFirstRecordAsHeader();
    }

    public static CSVFormat csvFormat() {
        return CSVFormat.DEFAULT;
    }
}
