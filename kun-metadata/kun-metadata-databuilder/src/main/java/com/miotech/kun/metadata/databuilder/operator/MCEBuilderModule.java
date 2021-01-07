package com.miotech.kun.metadata.databuilder.operator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

public class MCEBuilderModule extends AbstractModule {

    private final Props props;

    public MCEBuilderModule(Props props) {
        this.props = props;
    }

    @Provides
    @Singleton
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.get("datasource.jdbcUrl"));
        config.setUsername(props.get("datasource.username"));
        config.setPassword(props.get("datasource.password"));
        config.setDriverClassName(props.get("datasource.driverClassName"));
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(10));
        return new HikariDataSource(config);
    }

    @Provides
    @Singleton
    public Props buildProps() {
        return props;
    }

}
