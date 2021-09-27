package com.miotech.kun.workflow.worker.local;

import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.common.service.LineageService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;

public class LocalWorkerModule extends AppModule {
    private final Props props;

    public LocalWorkerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        install(new DatabaseModule());
        bind(MetadataServiceFacade.class).to(MetadataDatasetService.class);
        bind(LineageServiceFacade.class).to(LineageService.class);
        startNeo4jIfNeeded();



    }

    private void startNeo4jIfNeeded() {
        if (props.containsKey("neo4j.uri")) {
            install(new GraphDatabaseModule(props));
        }
    }
}
