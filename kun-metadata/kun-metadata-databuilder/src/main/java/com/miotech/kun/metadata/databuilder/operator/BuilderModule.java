package com.miotech.kun.metadata.databuilder.operator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.metadata.core.model.constant.OperatorKey.REDIS_HOST;
import static com.miotech.kun.metadata.core.model.constant.OperatorKey.STREAM_KEY;

public class BuilderModule extends AbstractModule {

    private final Props props;

    public BuilderModule(Props props) {
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
    public EventPublisher createEventPublisher() {
        return new RedisStreamEventPublisher(props.get(STREAM_KEY),
                RedisClient.create(String.format("redis://%s", props.get(REDIS_HOST))));
    }

    @Provides
    @Singleton
    public Props buildProps() {
        return props;
    }

    @Override
    protected void configure() {
        bind(Loader.class).to(PostgresLoader.class);
    }
}
