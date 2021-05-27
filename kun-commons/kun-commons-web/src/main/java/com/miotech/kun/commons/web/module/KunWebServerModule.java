package com.miotech.kun.commons.web.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.utils.Props;
import org.eclipse.jetty.server.Server;

public class KunWebServerModule extends AppModule {

    private final Props props;

    public KunWebServerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        install(new DatabaseModule());
        startNeo4jIfNeeded();
        startRpcIfNeeded();
    }

    @Provides
    @Singleton
    public Server getJettyServer() {
        return new Server();
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
