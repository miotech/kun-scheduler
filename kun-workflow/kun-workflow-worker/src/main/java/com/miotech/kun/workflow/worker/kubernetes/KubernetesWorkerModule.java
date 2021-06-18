package com.miotech.kun.workflow.worker.kubernetes;

import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.common.rpc.MetadataServiceFacadeImpl;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;

public class KubernetesWorkerModule extends AppModule {

    private final Props props;

    public KubernetesWorkerModule(Props props) {
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

}
