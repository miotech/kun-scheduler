package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class DataBuilderOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(DataBuilderOperator.class);
    private OperatorContext operatorContext;
    private static final String DEPLOY_MODE = "deploy-mode";
    private static final String DATASOURCE_ID = "datasourceId";
    private static final String DATASOURCE_JDBC_URL = "datasource.jdbcUrl";
    private static final String DATASOURCE_USERNAME = "datasource.username";
    private static final String DATASOURCE_PASSWORD = "datasource.password";
    private static final String DATASOURCE_DRIVER_CLASS_NAME = "datasource.driverClassName";

    @Override
    public void init() {
        operatorContext = getContext();
    }

    @Override
    public boolean run() {
        DataSource dataSource = null;
        try {
            Properties props = buildPropsFromVariable();

            Injector injector = Guice.createInjector(new DataSourceModule(props));
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

    private Properties buildPropsFromVariable() {
        Properties props = new Properties();
        props.setProperty(DATASOURCE_JDBC_URL, operatorContext.getConfig().getString(DATASOURCE_JDBC_URL));
        props.setProperty(DATASOURCE_USERNAME, operatorContext.getConfig().getString(DATASOURCE_USERNAME));
        props.setProperty(DATASOURCE_PASSWORD, operatorContext.getConfig().getString(DATASOURCE_PASSWORD));
        props.setProperty(DATASOURCE_DRIVER_CLASS_NAME, operatorContext.getConfig().getString(DATASOURCE_DRIVER_CLASS_NAME));

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
        configDef.define(DATASOURCE_ID, ConfigDef.Type.STRING, true, DATASOURCE_ID, DATASOURCE_ID);
        configDef.define("gid", ConfigDef.Type.STRING, true, "gid", "gid");
        return configDef;
    }

    @Override
    public void abort() {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
}
