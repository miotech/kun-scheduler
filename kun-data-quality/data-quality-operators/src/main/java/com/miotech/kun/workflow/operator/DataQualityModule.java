package com.miotech.kun.workflow.operator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.*;

public class DataQualityModule extends AbstractModule {

    private final Props props;

    public DataQualityModule(Props props) {
        this.props = props;
    }

    @Provides
    @Singleton
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.get(METADATA_DATASOURCE_URL));
        config.setUsername(props.get(METADATA_DATASOURCE_USERNAME));
        config.setPassword(props.get(METADATA_DATASOURCE_PASSWORD));
        config.setDriverClassName(props.get(METADATA_DATASOURCE_DIRVER_CLASS));
        return new HikariDataSource(config);
    }

    @Provides
    @Singleton
    public Props buildProps() {
        return props;
    }

}
