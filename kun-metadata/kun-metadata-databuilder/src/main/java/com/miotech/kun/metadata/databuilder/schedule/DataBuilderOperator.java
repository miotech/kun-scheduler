package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DataBuilderOperator extends KunOperator {
    private Logger logger;
    private OperatorContext operatorContext;

    @Override
    public void init() {
        logger = getContext().getLogger();
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

            String deployModeStr = operatorContext.getConfig().getString("deploy-mode");
            DataBuilderDeployMode deployMode = DataBuilderDeployMode.resolve(deployModeStr);
            switch (deployMode) {
                case ALL:
                    dataBuilder.buildAll();
                    break;
                case DATASOURCE:
                    Long datasourceId = Long.parseLong(operatorContext.getConfig().getString("datasourceId"));
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
        props.setProperty("datasource.jdbcUrl", operatorContext.getConfig().getString("datasource.jdbcUrl"));
        props.setProperty("datasource.username", operatorContext.getConfig().getString("datasource.username"));
        props.setProperty("datasource.password", operatorContext.getConfig().getString("datasource.password"));
        props.setProperty("datasource.driverClassName", operatorContext.getConfig().getString("datasource.driverClassName"));

        return props;
    }

    @Override
    public ConfigDef config() {
        ConfigDef configDef = new ConfigDef();
        configDef.define("datasource.jdbcUrl", ConfigDef.Type.STRING, true, "jdbcUrl", "jdbcUrl");
        configDef.define("datasource.username", ConfigDef.Type.STRING, true, "username", "username");
        configDef.define("datasource.password", ConfigDef.Type.STRING, true, "password", "password");
        configDef.define("datasource.driverClassName", ConfigDef.Type.STRING, true, "driverClassName", "driverClassName");
        configDef.define("deploy-mode", ConfigDef.Type.STRING, true, "deploy-mode", "deploy-mode");
        configDef.define("datasourceId", ConfigDef.Type.STRING, true, "datasourceId", "datasourceId");
        configDef.define("gid", ConfigDef.Type.STRING, true, "gid", "gid");
        return configDef;
    }

    @Override
    public void abort() {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
}
