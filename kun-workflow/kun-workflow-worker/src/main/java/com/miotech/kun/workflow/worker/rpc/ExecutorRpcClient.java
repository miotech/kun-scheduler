package com.miotech.kun.workflow.worker.rpc;

import com.google.common.base.Strings;
import com.google.protobuf.Empty;
import com.miotech.kun.workflow.facade.rpc.*;
import com.miotech.kun.workflow.worker.datasource.RpcConnectionConfig;
import com.miotech.kun.workflow.worker.datasource.RpcResultSetMetaData;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.sql.SQLException;

public class ExecutorRpcClient {

    private final ExecutorFacadeGrpc.ExecutorFacadeBlockingStub executorFacade;

    /**
     * @param host gRPC服务的主机名
     * @param port gRPC服务的端口
     */
    public ExecutorRpcClient(String host, int port) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port)
                // 使用非安全机制传输
                .usePlaintext()
                .build();

        executorFacade = ExecutorFacadeGrpc.newBlockingStub(managedChannel);
    }

    public Integer executeUpdate(RpcConnectionConfig connectionConfig, String sql) throws SQLException {
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setSql(sql)
                .setConnectionId(connectionConfig.getConnectionId())
                .setAutoCommit(connectionConfig.isAutoCommit())
                .build();
        UpdateResp resp = executorFacade.executeUpdate(updateRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getRows();
    }

    public String executeQuery(RpcConnectionConfig connectionConfig, String sql) throws SQLException {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setSql(sql)
                .setConnectionId(connectionConfig.getConnectionId())
                .setAutoCommit(connectionConfig.isAutoCommit())
                .build();
        QueryResp resp = executorFacade.executeQuery(queryRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getRsId();
    }

    public Boolean rsNext(String rsId) throws SQLException {
        RsNextRequest rsNextRequest = RsNextRequest.newBuilder().setRsId(rsId).build();
        RsNextResp resp = executorFacade.next(rsNextRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getHasNext();
    }

    public String getString(String rsId, Integer index) throws SQLException {
        RsGetString rsGetString = RsGetString.newBuilder().setRsId(rsId).setIndex(index.toString()).build();
        RsGetStringResp resp = executorFacade.getString(rsGetString);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getResult();
    }

    public String getString(String rsId, String label) throws SQLException {
        RsGetString rsGetString = RsGetString.newBuilder().setRsId(rsId).setLabel(label).build();
        RsGetStringResp resp = executorFacade.getString(rsGetString);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getResult();
    }

    public void closeRs(String rsId) throws SQLException {
        RsCloseRequest rsCloseRequest = RsCloseRequest.newBuilder().setRsId(rsId).build();
        RsCloseResp resp = executorFacade.closeRs(rsCloseRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
    }

    public RpcResultSetMetaData getRsMetaData(String rsId) throws SQLException {
        RsMetadataRequest rsMetadataRequest = RsMetadataRequest.newBuilder().setRsId(rsId).build();
        RsMetadataResp resp = executorFacade.getMetadata(rsMetadataRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return new RpcResultSetMetaData(resp);
    }

    /**
     * object format as json string
     */
    public String getObject(String rsId, Integer index, String type) throws SQLException {
        RsGetObjectRequest rsGetObjectRequest = RsGetObjectRequest.newBuilder()
                .setRsId(rsId)
                .setIndex(index.toString())
                .setType(type)
                .build();
        RsGetObjectResp resp = executorFacade.getObject(rsGetObjectRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getResult();
    }

    /**
     * object format as json string
     */
    public String getObject(String rsId, String label, String type) throws SQLException {
        RsGetObjectRequest rsGetObjectRequest = RsGetObjectRequest.newBuilder()
                .setRsId(rsId)
                .setLabel(label)
                .setType(type)
                .build();
        RsGetObjectResp resp = executorFacade.getObject(rsGetObjectRequest);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getResult();
    }

    public String getConnection() throws SQLException {
        GetConnectionResp resp = executorFacade.getConnection(Empty.newBuilder().build());
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
        return resp.getConnectionId();
    }

    public void closeConnection(String connectionId) throws SQLException {
        CloseConnection closeConnection = CloseConnection.newBuilder()
                .setConnectionId(connectionId)
                .build();
        CloseConnectionResp resp = executorFacade.closeConnection(closeConnection);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
    }

    public void commitConnection(String connectionId) throws SQLException {
        CommitConnection commitConnection = CommitConnection.newBuilder()
                .setConnectionId(connectionId)
                .build();
        CommitConnectionResp resp = executorFacade.commitConnection(commitConnection);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
    }

    public void rollbackConnection(String connectionId) throws SQLException {
        RollBackConnection rollBackConnection = RollBackConnection.newBuilder()
                .setConnectionId(connectionId)
                .build();
        RollBackConnectionResp resp = executorFacade.rollBackConnection(rollBackConnection);
        String exception = resp.getException();
        if (!Strings.isNullOrEmpty(exception)) {
            throw new SQLException(exception);
        }
    }


}
