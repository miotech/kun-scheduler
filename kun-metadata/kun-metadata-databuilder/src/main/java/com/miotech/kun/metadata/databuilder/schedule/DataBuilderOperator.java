package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.core.execution.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.*;

public class DataBuilderOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(DataBuilderOperator.class);
    private OperatorContext operatorContext;

    @Override
    public void init() {
        operatorContext = getContext();
    }

    @Override
    public boolean run() {
        DataSource dataSource = null;
        try {
            Props props = buildPropsFromVariable();

            Injector injector = Guice.createInjector(new DataSourceModule(props), new PropertiesModule(props));
            dataSource = injector.getInstance(DataSource.class);
            DataBuilder dataBuilder = injector.getInstance(DataBuilder.class);

            String deployModeStr = operatorContext.getConfig().getString(DEPLOY_MODE);
            DataBuilderDeployMode deployMode = DataBuilderDeployMode.resolve(deployModeStr);
            switch (deployMode) {
                case ALL:
                    dataBuilder.buildAll();
                    break;
                case DATASOURCE:
                    Long datasourceId = Long.parseLong(operatorContext.getConfig().getString(DATASOURCE_ID));
                    dataBuilder.buildDatasource(datasourceId);
                    break;
                case DATASET:
                    Long gid = Long.parseLong(operatorContext.getConfig().getString("gid"));
                    dataBuilder.buildDataset(gid);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid deployMode: " + deployModeStr);
            }

            return true;
        } catch (Exception e) {
            logger.error("DataBuilderOperator run error:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
    }

    private Props buildPropsFromVariable() {
        Props props = new Props();
        props.put(DATASOURCE_JDBC_URL, operatorContext.getConfig().getString(DATASOURCE_JDBC_URL));
        props.put(DATASOURCE_USERNAME, operatorContext.getConfig().getString(DATASOURCE_USERNAME));
        props.put(DATASOURCE_PASSWORD, operatorContext.getConfig().getString(DATASOURCE_PASSWORD));
        props.put(DATASOURCE_DRIVER_CLASS_NAME, operatorContext.getConfig().getString(DATASOURCE_DRIVER_CLASS_NAME));
        props.put(EXTRACT_STATS, operatorContext.getConfig().getBoolean(EXTRACT_STATS).toString());
        return props;
    }

    @Override
    public ConfigDef config() {
        ConfigDef configDef = new ConfigDef();
        configDef.define(DATASOURCE_JDBC_URL, ConfigDef.Type.STRING, true, "jdbcUrl", "jdbcUrl");
        configDef.define(DATASOURCE_USERNAME, ConfigDef.Type.STRING, true, "username", "username");
        configDef.define(DATASOURCE_PASSWORD, ConfigDef.Type.STRING, true, "password", "password");
        configDef.define(DATASOURCE_DRIVER_CLASS_NAME, ConfigDef.Type.STRING, true, "driverClassName", "driverClassName");
        configDef.define(DEPLOY_MODE, ConfigDef.Type.STRING, true, DEPLOY_MODE, DEPLOY_MODE);
        configDef.define(DATASOURCE_ID, ConfigDef.Type.STRING, "", true, DATASOURCE_ID, DATASOURCE_ID);
        configDef.define(GID, ConfigDef.Type.STRING, "", true, GID, GID);
        configDef.define(EXTRACT_STATS, ConfigDef.Type.BOOLEAN, false, true, EXTRACT_STATS, EXTRACT_STATS);
        return configDef;
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

    @Override
    public void abort() {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
}
