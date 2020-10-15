package com.miotech.kun.workflow.executor.rpc;

import com.miotech.kun.commons.rpc.DefaultRpcClusterInvoker;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;

public class WorkerClusterInvoker<T> extends DefaultRpcClusterInvoker<T> {

    public WorkerClusterInvoker(Directory<T> directory){
        super(directory);
    }


    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {

        Integer port = (Integer) RpcContext.getContext().get("port");
        if (port == null){
            throw new RuntimeException("port is blank ");
        }
        //2.检查是否有可用invoker
        checkInvokers(invokers, invocation);
        Invoker<T> invoked = invokers.stream().filter(invoker -> invoker.getUrl().getPort() == port)
                .findFirst().orElse(null);
        if (null == invoked) {
            throw new RpcException(RpcException.NO_INVOKER_AVAILABLE_AFTER_FILTER,
                    "Failed to invoke the method " + invocation.getMethodName() + " in the service "
                            + getInterface().getName() + ". No provider available for the service "
                            + directory.getUrl().getServiceKey() + " from worker port " + port + " on the consumer "
                            + NetUtils.getLocalHost());
        }
        try {

            return invoked.invoke(invocation);
        } catch (Throwable e) {
            if (e instanceof RpcException && ((RpcException) e).isBiz()) { // biz exception.
                throw (RpcException) e;
            }
            throw new RpcException(e instanceof RpcException ? ((RpcException) e).getCode() : 0,
                    "Fail invoke providers " + (invoked != null ? invoked.getUrl() : "") + " " + loadbalance.getClass().getSimpleName()
                            + " select from all providers " + invokers + " for service " + getInterface().getName()
                            + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost()
                            + " use dubbo version " + Version.getVersion()
                            + ", but no luck to perform the invocation. Last error is: " + e.getMessage(),
                    e.getCause() != null ? e.getCause() : e);
        }
    }

}
