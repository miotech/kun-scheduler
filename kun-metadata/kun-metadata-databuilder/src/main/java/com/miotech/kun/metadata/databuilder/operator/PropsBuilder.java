package com.miotech.kun.metadata.databuilder.operator;

import com.miotech.kun.commons.utils.MapProps;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsProvider;
import com.miotech.kun.workflow.core.execution.OperatorContext;

import java.util.HashMap;
import java.util.Map;

import static com.miotech.kun.metadata.core.model.constant.OperatorKey.*;
import static com.miotech.kun.metadata.core.model.constant.OperatorKey.DATASOURCE_DRIVER_CLASS_NAME;

public class PropsBuilder {

    public static Props putConn(OperatorContext operatorContext) {
        Props props = new Props();
        Map<String,Object> map = new HashMap<>();
        map.put(DATASOURCE_JDBC_URL, operatorContext.getConfig().getString(DATASOURCE_JDBC_URL));
        map.put(DATASOURCE_USERNAME, operatorContext.getConfig().getString(DATASOURCE_USERNAME));
        map.put(DATASOURCE_PASSWORD, operatorContext.getConfig().getString(DATASOURCE_PASSWORD));
        map.put(DATASOURCE_DRIVER_CLASS_NAME, operatorContext.getConfig().getString(DATASOURCE_DRIVER_CLASS_NAME));
        PropsProvider runTimeProvider = new MapProps(map);
        props.addPropsProvider(runTimeProvider);
        return props;
    }

}
