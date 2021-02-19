package com.miotech.kun.metadata.databuilder.operator;

import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.builder.MSEBuilder;
import com.miotech.kun.metadata.databuilder.constant.StatisticsMode;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.workflow.core.execution.*;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.*;

public class MSEOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(MSEOperator.class);
    private Props props;

    @Override
    public void init() {
        props = buildPropsFromVariable(getContext());
        ApplicationContext.init(props);
    }

    @Override
    public boolean run() {
        DataSource dataSource = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("MSEOperator use props: {}", ApplicationContext.getContext().getProps());
            }

            Injector injector = ApplicationContext.getContext().getInjector();
            dataSource = injector.getInstance(DataSource.class);
            MSEBuilder dataBuilder = injector.getInstance(MSEBuilder.class);
            dataBuilder.extractStatistics(Long.parseLong(props.getString(GID)),
                    Long.parseLong(props.getString(SNAPSHOT_ID)),
                    StatisticsMode.valueOf(props.getString(STATISTICS_MODE).toUpperCase()));

            return true;
        } catch (Exception e) {
            logger.error("MSEOperator run error:", e);
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
                .define(GID, ConfigDef.Type.STRING, true, GID, GID)
                .define(SNAPSHOT_ID, ConfigDef.Type.STRING, true, SNAPSHOT_ID, SNAPSHOT_ID)
                .define(STATISTICS_MODE, ConfigDef.Type.STRING, StatisticsMode.TABLE.name(), true, STATISTICS_MODE, STATISTICS_MODE);
        return configDef;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    private Props buildPropsFromVariable(OperatorContext operatorContext) {
        Props props = PropsBuilder.putConn(operatorContext);

        props.put(GID, operatorContext.getConfig().getString(GID));
        props.put(SNAPSHOT_ID, operatorContext.getConfig().getString(SNAPSHOT_ID));
        props.put(STATISTICS_MODE, operatorContext.getConfig().getString(STATISTICS_MODE));
        return props;
    }

}
