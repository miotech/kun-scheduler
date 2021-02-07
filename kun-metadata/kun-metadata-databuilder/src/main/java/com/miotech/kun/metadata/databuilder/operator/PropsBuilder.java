package com.miotech.kun.metadata.databuilder.operator;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.execution.OperatorContext;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.*;
import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.DATASOURCE_DRIVER_CLASS_NAME;

public class PropsBuilder {

    public static Props putConn(OperatorContext operatorContext) {
        Props props = new Props();
        props.put(DATASOURCE_JDBC_URL, operatorContext.getConfig().getString(DATASOURCE_JDBC_URL));
        props.put(DATASOURCE_USERNAME, operatorContext.getConfig().getString(DATASOURCE_USERNAME));
        props.put(DATASOURCE_PASSWORD, operatorContext.getConfig().getString(DATASOURCE_PASSWORD));
        props.put(DATASOURCE_DRIVER_CLASS_NAME, operatorContext.getConfig().getString(DATASOURCE_DRIVER_CLASS_NAME));

        return props;
    }

}
