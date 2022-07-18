package com.miotech.kun.workflow.common.rpc;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.Empty;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.facade.rpc.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutorFacadeImpl extends ExecutorFacadeGrpc.ExecutorFacadeImplBase {

    private Logger logger = LoggerFactory.getLogger(ExecutorFacadeImpl.class);
    private Map<String, Connection> connectionPool = new HashMap<>();
    private Map<String, PreparedStatement> statementPool = new HashMap<>();
    private Map<String, ResultSet> resultSetMapPool = new HashMap<>();
    private final Integer FETCH_SIZE = 1000;

    @Inject
    @Named("executorDatasource")
    private DataSource dataSource;

    @Override
    public void executeQuery(QueryRequest queryRequest, StreamObserver<QueryResp> responseObserver) {
        String sql = queryRequest.getSql();
        try {
            String connectionId = queryRequest.getConnectionId();
            Connection connection = connectionPool.get(connectionId);
            connection.setAutoCommit(queryRequest.getAutoCommit());

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setFetchSize(FETCH_SIZE);

            ResultSet resultSet = preparedStatement.executeQuery();
            String rsId = String.valueOf(IdGenerator.getInstance().nextId());

            statementPool.put(rsId, preparedStatement);
            resultSetMapPool.put(rsId, resultSet);
            QueryResp queryResp = QueryResp.newBuilder().setRsId(rsId).build();
            responseObserver.onNext(queryResp);
        } catch (Throwable e) {
            logger.error("execute sql {} query failed", sql, e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void executeUpdate(UpdateRequest updateRequest, StreamObserver<UpdateResp> responseObserver) {
        String sql = updateRequest.getSql();
        try (Connection connection = dataSource.getConnection()) {
            int rows = connection.prepareStatement(sql).executeUpdate();
            UpdateResp resp = UpdateResp.newBuilder().setRows(rows).build();
            responseObserver.onNext(resp);
        } catch (SQLException e) {
            logger.error("execute sql {} update failed", sql, e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void next(RsNextRequest rsNextRequest, StreamObserver<RsNextResp> responseObserver) {
        String rsId = rsNextRequest.getRsId();
        ResultSet resultSet = resultSetMapPool.get(rsId);
        if (resultSet == null) {
            logger.error("resultSet with id = {} ,not exits", rsId);
        }
        try {
            Boolean hasNext = resultSet.next();
            RsNextResp resp = RsNextResp.newBuilder().setHasNext(hasNext).build();
            responseObserver.onNext(resp);
        } catch (Throwable e) {
            logger.error("get next of resultSet failed", e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getString(RsGetString rsGetString, StreamObserver<RsGetStringResp> responseObserver) {
        String rsId = rsGetString.getRsId();
        ResultSet resultSet = resultSetMapPool.get(rsId);
        if (resultSet == null) {
            logger.error("resultSet with id = {} ,not exits", rsId);
        }
        String result = null;
        if (!Strings.isNullOrEmpty(rsGetString.getIndex())) {
            try {
                result = resultSet.getString(Integer.valueOf(rsGetString.getIndex()));
            } catch (Throwable e) {
                logger.error("failed to get string by index : {}", rsGetString.getIndex(), e);
            }
        } else {
            try {
                result = resultSet.getString(rsGetString.getLabel());
            } catch (Throwable e) {
                logger.error("failed to get string by lable : {}", rsGetString.getLabel(), e);
            }
        }
        RsGetStringResp.Builder respOrBuilder = RsGetStringResp.newBuilder();
        if (result != null) {
            respOrBuilder.setResult(result);
        }
        RsGetStringResp resp = respOrBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    @Override
    public void closeRs(RsCloseRequest rsCloseRequest, StreamObserver<RsCloseResp> responseObserver) {
        String rsId = rsCloseRequest.getRsId();
        ResultSet resultSet = resultSetMapPool.get(rsId);
        PreparedStatement statement = statementPool.get(rsId);
        try {
            if(resultSet != null){
                resultSet.close();
            }
            if(statement != null){
                statement.close();
            }
            resultSetMapPool.remove(rsId);
            statementPool.remove(rsId);
            RsCloseResp rsCloseResp = RsCloseResp.newBuilder().setIsClosed(true).build();
            responseObserver.onNext(rsCloseResp);
        } catch (SQLException e) {
            logger.error("failed to close resultSet , rsId = {}", rsCloseRequest.getRsId(), e);
        }
        responseObserver.onCompleted();

    }

    @Override
    public void getMetadata(RsMetadataRequest rsMetadataRequest, StreamObserver<RsMetadataResp> responseObserver) {
        String rsId = rsMetadataRequest.getRsId();
        ResultSet resultSet = resultSetMapPool.get(rsId);
        if (resultSet == null) {
            logger.error("resultSet with id = {} ,not exits", rsId);
        }
        List<String> columNameList = new ArrayList<>();
        List<String> columnLabelList = new ArrayList<>();
        Integer columnCount = 0;
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columNameList.add(columnName);
                String columnLabel = metaData.getColumnLabel(i);
                columnLabelList.add(columnLabel);
            }
        } catch (Throwable e) {
            logger.error("failed to get metadata request is {}", "", e);
        }
        RsMetadataResp.Builder respBuilder = RsMetadataResp.newBuilder();
        respBuilder.setColumnCount(columnCount);
        respBuilder.addAllColumnName(columNameList);
        respBuilder.addAllColumnLabel(columnLabelList);
        RsMetadataResp resp = respBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    @Override
    public void getObject(RsGetObjectRequest request, StreamObserver<RsGetObjectResp> responseObserver) {
        String rsId = request.getRsId();
        ResultSet resultSet = resultSetMapPool.get(rsId);
        if (resultSet == null) {
            logger.error("resultSet with id = {} ,not exits", rsId);
        }
        String result = null;
        String type = request.getType();
        try {
            Class clazz = Class.forName(type);
            if (!Strings.isNullOrEmpty(request.getIndex())) {
                result = JSONUtils.toJsonString(resultSet.getObject(Integer.valueOf(request.getIndex()), clazz));
            } else {
                result = JSONUtils.toJsonString(resultSet.getObject(request.getLabel(), clazz));
            }
        } catch (Throwable e) {
            logger.error("failed to get object,request is {}", request, e);
        }

        RsGetObjectResp.Builder respOrBuilder = RsGetObjectResp.newBuilder();
        if (result != null) {
            respOrBuilder.setResult(result);
        }
        RsGetObjectResp resp = respOrBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    @Override
    public void getConnection(Empty request, StreamObserver<GetConnectionResp> responseObserver) {
        GetConnectionResp.Builder respBuilder = GetConnectionResp.newBuilder();
        try {
            Connection connection = dataSource.getConnection();
            String connectionId = String.valueOf(IdGenerator.getInstance().nextId());
            connectionPool.put(connectionId, connection);
            respBuilder.setConnectionId(connectionId);
        } catch (Throwable e) {
            logger.error("failed to get connection from datasource", e);
            respBuilder.setException(e.getMessage());
        }
        GetConnectionResp resp = respBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void closeConnection(CloseConnection request, StreamObserver<CloseConnectionResp> responseObserver) {
        CloseConnectionResp.Builder respBuilder = CloseConnectionResp.newBuilder();
        String connectionId = request.getConnectionId();
        try {
            Connection connection = connectionPool.get(connectionId);
            connection.close();
            connectionPool.remove(connectionId);
        }catch (Throwable e){
            logger.error("failed to close connection", e);
            respBuilder.setException(e.getMessage());
        }
        CloseConnectionResp resp = respBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void commitConnection(CommitConnection request, StreamObserver<CommitConnectionResp> responseObserver) {
        CommitConnectionResp.Builder respBuilder = CommitConnectionResp.newBuilder();
        String connectionId = request.getConnectionId();
        try {
            Connection connection = connectionPool.get(connectionId);
            connection.commit();
        }catch (Throwable e){
            logger.error("failed to commit connection", e);
            respBuilder.setException(e.getMessage());
        }
        CommitConnectionResp resp = respBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void rollBackConnection(RollBackConnection request, StreamObserver<RollBackConnectionResp> responseObserver){
        RollBackConnectionResp.Builder respBuilder = RollBackConnectionResp.newBuilder();
        String connectionId = request.getConnectionId();
        try {
            Connection connection = connectionPool.get(connectionId);
            connection.rollback();
        }catch (Throwable e){
            logger.error("failed to rollback connection", e);
            respBuilder.setException(e.getMessage());
        }
        RollBackConnectionResp resp = respBuilder.build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }


}
