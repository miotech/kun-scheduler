package com.miotech.kun.workflow.worker.kubernetes;

import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.common.service.LineageService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.worker.datasource.RpcDatasource;
import com.miotech.kun.workflow.worker.rpc.ExecutorRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class KubernetesWorkerModule extends AppModule {

    private final Props props;

    private final Logger logger = LoggerFactory.getLogger(KubernetesWorkerModule.class);

    public KubernetesWorkerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        String rpcHost = props.get("executorRpcHost");
        Integer rpcPort = props.getInt("executorRpcPort");
        logger.info("init rpc client...");
        ExecutorRpcClient executorRpcClient = new ExecutorRpcClient(rpcHost, rpcPort);
        RpcDatasource rpcDatasource = new RpcDatasource(executorRpcClient);
        bind(DataSource.class).toInstance(rpcDatasource);
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
