package com.miotech.kun.commons.web.module;

import com.google.inject.AbstractModule;
import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.utils.Props;

public class CommonModule extends AbstractModule {
    private final Props props;

    public CommonModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        install(new DatabaseModule());
        startNeo4jIfNeeded();
        startRpcIfNeeded();
    }
    
    private void startNeo4jIfNeeded() {
        if (props.containsKey("neo4j.uri")) {
            install(new GraphDatabaseModule(props));
        }
    }
    
    private void startRpcIfNeeded() {
        if (props.containsKey("rpc.registry")) {
            install(new RpcModule(props));
        }
    }
}
