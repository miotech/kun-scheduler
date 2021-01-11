package com.miotech.kun.metadata.databuilder.operator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.builder.MSEBuilder;
import com.miotech.kun.workflow.core.execution.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.*;

public class MSEOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(MSEOperator.class);

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
                logger.debug("MSEOperator use operatorContext: {}", operatorContext.getConfig());
            }

            Props props = buildPropsFromVariable();
            Injector injector = Guice.createInjector(new DataSourceModule(props));
            dataSource = injector.getInstance(DataSource.class);
            MSEBuilder dataBuilder = injector.getInstance(MSEBuilder.class);
            dataBuilder.extractStat(Long.parseLong(operatorContext.getConfig().getString("gid")));

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
                .define(GID, ConfigDef.Type.STRING, "", true, GID, GID);
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
        return props;
    }

}
