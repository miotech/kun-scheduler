package com.miotech.kun.workflow.worker.local;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.rpc.RpcConsumer;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.common.rpc.MetadataServiceFacadeImpl;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;

public class LocalWorkerModule extends AppModule {
    private final Props props;

    private final Integer RPC_TIMEOUT = 5000;

    public LocalWorkerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        install(new DatabaseModule());
        bind(MetadataServiceFacade.class).to(MetadataServiceFacadeImpl.class);
        startNeo4jIfNeeded();



    }

    private void startNeo4jIfNeeded() {
        if (props.containsKey("neo4j.uri")) {
            install(new GraphDatabaseModule(props));
        }
    }

    @Singleton
    @Provides
    public WorkflowExecutorFacade workflowExecutorFacade(RpcConsumer rpcConsumer) {
        rpcConsumer.setTimeout(RPC_TIMEOUT);
        return rpcConsumer.getService("default", WorkflowExecutorFacade.class, "1.0");
    }
}
