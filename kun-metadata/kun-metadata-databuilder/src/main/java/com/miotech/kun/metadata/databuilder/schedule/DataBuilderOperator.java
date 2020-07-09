package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;

import java.util.Properties;

public class DataBuilderOperator extends Operator {
    private Logger logger;
    private OperatorContext operatorContext;

    @Override
    public void init() {
        logger = getContext().getLogger();
        operatorContext = getContext();
    }

    @Override
    public boolean run() {
        try {
            Properties props = buildPropsFromVariable();

            Injector injector = Guice.createInjector(new DataSourceModule(props));
            DataBuilder dataBuilder = injector.getInstance(DataBuilder.class);

            String deployModeStr = operatorContext.getVariable("deploy-mode");
            DataBuilderDeployMode deployMode = DataBuilderDeployMode.resolve(deployModeStr);
            switch (deployMode) {
                case ALL:
                    dataBuilder.buildAll();
                    break;
                case DATASOURCE:
                    Long datasourceId = Long.parseLong(operatorContext.getVariable("datasourceId"));
                    dataBuilder.buildDatasource(datasourceId);
                    break;
                case DATASET:
                    Long gid = Long.parseLong(operatorContext.getVariable("gid"));
                    dataBuilder.buildDataset(gid);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid deployMode: " + deployModeStr);
            }

            return true;
        } catch (Exception e) {
            logger.error("DataBuilderOperator run error:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    private Properties buildPropsFromVariable() {
        Properties props = new Properties();
        props.setProperty("datasource.jdbcUrl", operatorContext.getVariable("datasource.jdbcUrl"));
        props.setProperty("datasource.username", operatorContext.getVariable("datasource.username"));
        props.setProperty("datasource.password", operatorContext.getVariable("datasource.password"));
        props.setProperty("datasource.driverClassName", operatorContext.getVariable("datasource.driverClassName"));

        return props;
    }
}
