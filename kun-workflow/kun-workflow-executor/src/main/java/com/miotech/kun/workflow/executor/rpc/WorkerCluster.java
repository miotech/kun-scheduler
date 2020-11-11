package com.miotech.kun.workflow.executor.rpc;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

public class WorkerCluster implements Cluster {

    public final static String NAME = "workerCluster";

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new WorkerClusterInvoker<>(directory);
    }
}
