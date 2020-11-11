package com.miotech.kun.workflow.executor.rpc;

import com.miotech.kun.commons.rpc.RpcConsumer;

public class WorkerClusterConsumer extends RpcConsumer {

    @Override
    protected String configCluster(){
        return WorkerCluster.NAME;
    }
}
