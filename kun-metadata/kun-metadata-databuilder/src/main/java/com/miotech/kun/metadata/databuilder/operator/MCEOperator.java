package com.miotech.kun.metadata.databuilder.operator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.builder.MCEBuilder;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.core.execution.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.*;

public class MCEOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(MCEOperator.class);
    private OperatorContext operatorContext;

    @Override
    public void init() {
        operatorContext = getContext();
    }

    @Override
    public boolean run() {
        DataSource dataSource = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("MCEOperator use operatorContext: {}", operatorContext.getConfig());
            }

            Props props = buildPropsFromVariable();
            Injector injector = Guice.createInjector(new BuilderModule(props));
            dataSource = injector.getInstance(DataSource.class);
            MCEBuilder dataBuilder = injector.getInstance(MCEBuilder.class);

            String deployModeStr = operatorContext.getConfig().getString(DEPLOY_MODE);
            DataBuilderDeployMode deployMode = DataBuilderDeployMode.resolve(deployModeStr);
            switch (deployMode) {
                case DATASOURCE:
                    Long datasourceId = Long.parseLong(operatorContext.getConfig().getString(DATASOURCE_ID));
                    dataBuilder.extractSchemaOfDataSource(datasourceId);
                    break;
                case DATASET:
                    Long gid = Long.parseLong(operatorContext.getConfig().getString(GID));
                    dataBuilder.extractSchemaOfDataset(gid);
                    break;
                case PUSH:
                    dataBuilder.extractSchemaOfPush(operatorContext.getConfig().getString(MCE));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid deployMode: " + deployModeStr);
            }

            return true;
        } catch (Exception e) {
            logger.error("MCEOperator run error:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigDef config() {
        ConfigDef configDef = new ConfigDef();
        configDef.define(DATASOURCE_JDBC_URL, ConfigDef.Type.STRING, true, "jdbcUrl", "jdbcUrl")
                .define(DATASOURCE_USERNAME, ConfigDef.Type.STRING, true, "username", "username")
                .define(DATASOURCE_PASSWORD, ConfigDef.Type.STRING, true, "password", "password")
                .define(DATASOURCE_DRIVER_CLASS_NAME, ConfigDef.Type.STRING, true, "driverClassName", "driverClassName")
                .define(DEPLOY_MODE, ConfigDef.Type.STRING, true, DEPLOY_MODE, DEPLOY_MODE)
                .define(DATASOURCE_ID, ConfigDef.Type.STRING, "", true, DATASOURCE_ID, DATASOURCE_ID)
                .define(BROKERS, ConfigDef.Type.STRING, "", true, BROKERS, BROKERS)
                .define(MSE_TOPIC, ConfigDef.Type.STRING, "", true, MSE_TOPIC, MSE_TOPIC)
                .define(GID, ConfigDef.Type.STRING, "", true, GID, GID)
                .define(MCE, ConfigDef.Type.STRING, "", true, MCE, MCE);
        return configDef;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    private Props buildPropsFromVariable() {
        Props props = new Props();
        props.put(DATASOURCE_JDBC_URL, operatorContext.getConfig().getString(DATASOURCE_JDBC_URL));
        props.put(DATASOURCE_USERNAME, operatorContext.getConfig().getString(DATASOURCE_USERNAME));
        props.put(DATASOURCE_PASSWORD, operatorContext.getConfig().getString(DATASOURCE_PASSWORD));
        props.put(DATASOURCE_DRIVER_CLASS_NAME, operatorContext.getConfig().getString(DATASOURCE_DRIVER_CLASS_NAME));

        props.put(BROKERS, operatorContext.getConfig().getString(BROKERS));
        props.put(MSE_TOPIC, operatorContext.getConfig().getString(MSE_TOPIC));
        return props;
    }

}
