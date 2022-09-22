package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLUtils;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.miotech.kun.commons.db.sql.SQLUtils.and;
import static com.miotech.kun.commons.db.sql.SQLUtils.set;

/**
 * @program: kun
 * @description: connection
 * @author: zemin  huang
 * @create: 2022-09-13 09:54
 **/
@Singleton
public class ConnectionDao {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionDao.class);
    @Inject
    private DatabaseOperator dbOperator;

    private static final String TABLE_KMCI = "kun_mt_connection_info";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATASOURCE_ID = "datasource_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONN_SCOPE = "conn_scope";
    private static final String COLUMN_CONN_CONFIG = "conn_config";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_UPDATE_USER = "update_user";
    private static final String COLUMN_UPDATED_TIME = "update_time";
    private static final String COLUMN_CREATE_USER = "create_user";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_DELETED = "deleted";
    private static final String[] COLUMNS = {
            COLUMN_ID,
            COLUMN_DATASOURCE_ID,
            COLUMN_NAME,
            COLUMN_CONN_SCOPE,
            COLUMN_CONN_CONFIG,
            COLUMN_DESCRIPTION,
            COLUMN_UPDATE_USER,
            COLUMN_UPDATED_TIME,
            COLUMN_CREATE_USER,
            COLUMN_CREATE_TIME,
            COLUMN_DELETED
    };


    private ConnectionInfo updateConnectionInfo(ConnectionInfo connectionInfo) {
        Preconditions.checkNotNull(connectionInfo);
        return saveOrUpdateConnection(connectionInfo.getDatasourceId(), connectionInfo);
    }

    public ConnectionInfo addConnection(final Long datasourceId, final ConnectionInfo connectionInfo) {
        Preconditions.checkNotNull(datasourceId);
        Preconditions.checkNotNull(connectionInfo);
        Optional<ConnectionInfo> duplicateName = getDuplicateName(datasourceId, connectionInfo.getName());
        if (duplicateName.isPresent()) {
            throw new IllegalArgumentException(String.format("connection name exist,same conn name:%s", duplicateName.get().getName()));
        }
        OffsetDateTime now = DateTimeUtils.now();
        ConnectionInfo insertInfo = connectionInfo
                .cloneBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withDatasourceId(datasourceId)
                .withCreateTime(now)
                .withUpdateTime(now)
                .withDeleted(false)
                .build();
        Optional<ConnectionInfo> duplicate = getDuplicateConnectionConfig(insertInfo);
        if (duplicate.isPresent()) {
            return duplicate.get();
        }


        List<Object> params = Lists.newArrayList();
        params.add(insertInfo.getId());
        params.add(insertInfo.getDatasourceId());
        params.add(insertInfo.getName());
        params.add(insertInfo.getConnScope().name());
        params.add(JSONUtils.toJsonString(insertInfo.getConnectionConfigInfo()));
        params.add(insertInfo.getDescription());
        params.add(insertInfo.getUpdateUser());
        params.add(insertInfo.getUpdateTime());
        params.add(insertInfo.getCreateUser());
        params.add(insertInfo.getCreateTime());
        params.add(insertInfo.getDeleted());
        String sql = DefaultSQLBuilder.newBuilder().insert(COLUMNS).into(TABLE_KMCI).asPrepared().getSQL();
        dbOperator.update(sql, params.toArray());
        return insertInfo;
    }

    private Optional<ConnectionInfo> getDuplicateConnectionConfig(ConnectionInfo updateConnectionInfo) {
        List<ConnectionInfo> connectionInfoList = fetchDatasourceConnectionList(updateConnectionInfo.getDatasourceId(), updateConnectionInfo.getConnScope());
        if (CollectionUtils.isEmpty(connectionInfoList)) {
            return Optional.empty();
        }
        if (ConnScope.USER_CONN.equals(updateConnectionInfo.getConnScope())) {
            return connectionInfoList.stream()
                    .filter(connectionInfo -> connectionInfo.getConnectionConfigInfo().equals(updateConnectionInfo.getConnectionConfigInfo()))
                    .findFirst();
        } else {
            return connectionInfoList.stream().findFirst();

        }
    }

    private Optional<ConnectionInfo> getDuplicateName(Long datasourceId, String connName) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_DATASOURCE_ID, COLUMN_CONN_SCOPE, COLUMN_NAME, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMCI).where(SQLUtils.and(options)).getSQL();
        return Optional.ofNullable(dbOperator.fetchOne(sql, ConnectionDao.ConnectionInfoMapper.INSTANCE, datasourceId, ConnScope.USER_CONN.name(), connName, false));
    }


    public ConnectionInfo saveOrUpdateConnection(final Long datasourceId, final ConnectionInfo connectionInfo) {
        Preconditions.checkNotNull(connectionInfo);
        Preconditions.checkNotNull(datasourceId);
        if (Objects.isNull(connectionInfo.getId())) {
            return addConnection(datasourceId, connectionInfo);
        }
        Optional<ConnectionInfo> duplicateConnectionInfo = getDuplicateConnectionConfig(connectionInfo);
        if (duplicateConnectionInfo.isPresent() && (!connectionInfo.getId().equals(duplicateConnectionInfo.get().getId()))) {
            throw new IllegalArgumentException(String.format("connection config exist,same conn name:%s", duplicateConnectionInfo.get().getName()));
        }
        Optional<ConnectionInfo> duplicateName = getDuplicateName(datasourceId, connectionInfo.getName());
        if (duplicateName.isPresent() && (!connectionInfo.getId().equals(duplicateName.get().getId()))) {
            throw new IllegalArgumentException(String.format("connection name exist,same conn name:%s", duplicateName.get().getName()));
        }
        return update(connectionInfo);

    }

    private ConnectionInfo update(ConnectionInfo connectionInfo) {
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_DATASOURCE_ID, COLUMN_NAME, COLUMN_CONN_CONFIG,
                COLUMN_DESCRIPTION, COLUMN_UPDATE_USER, COLUMN_UPDATED_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_ID, COLUMN_DELETED);
        OffsetDateTime now = DateTimeUtils.now();
        ConnectionInfo updateInfo = connectionInfo.cloneBuilder().withDeleted(false).withUpdateTime(now).build();

        List<Object> params = Lists.newArrayList();
        params.add(updateInfo.getDatasourceId());
        params.add(updateInfo.getName());
        params.add(JSONUtils.toJsonString(updateInfo.getConnectionConfigInfo()));
        params.add(updateInfo.getDescription());
        params.add(updateInfo.getUpdateUser());
        params.add(updateInfo.getUpdateTime());
        params.add(updateInfo.getId());
        params.add(updateInfo.getDeleted());

        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KMCI)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
        return updateInfo;
    }

    public Optional<ConnectionInfo> findConnection(Long connId) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_ID, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMCI).where(SQLUtils.and(options)).getSQL();
        return Optional.ofNullable(dbOperator.fetchOne(sql, ConnectionDao.ConnectionInfoMapper.INSTANCE, connId, false));
    }

    public Optional<DatasourceConnection> fetchDatasourceConnection(Long datasourceId) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_DATASOURCE_ID, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMCI).where(SQLUtils.and(options)).getSQL();
        List<ConnectionInfo> connectionInfos = dbOperator.fetchAll(sql, ConnectionInfoMapper.INSTANCE, datasourceId, false);
        if (CollectionUtils.isEmpty(connectionInfos)) {
            return Optional.empty();
        }
        return Optional.of(new DatasourceConnection(connectionInfos));
    }

    public List<ConnectionInfo> fetchDatasourceConnectionList(Long datasourceId, ConnScope connScope) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_DATASOURCE_ID, COLUMN_CONN_SCOPE, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMCI).where(SQLUtils.and(options)).getSQL();
        return dbOperator.fetchAll(sql, ConnectionInfoMapper.INSTANCE, datasourceId, connScope.name(), false);
    }

    public List<ConnectionInfo> fetchConnScopeConnection(ConnScope connScope) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_CONN_SCOPE, COLUMN_DELETED);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_KMCI).where(SQLUtils.and(options)).getSQL();
        return dbOperator.fetchAll(sql, ConnectionInfoMapper.INSTANCE, connScope.name(), false);

    }

    public ConnectionInfo removeConnection(ConnectionInfo connectionInfo) {
        Preconditions.checkNotNull(connectionInfo);
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_DELETED, COLUMN_UPDATED_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_ID);
        OffsetDateTime now = DateTimeUtils.now();
        ConnectionInfo deleted = connectionInfo.cloneBuilder().withDeleted(true).withUpdateTime(now).build();
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                deleted.getDeleted(),
                deleted.getUpdateTime(),
                deleted.getId()
        );
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KMCI)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
        return deleted;
    }

    public void removeConnection(Long connId) {
        Preconditions.checkNotNull(connId);
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_DELETED, COLUMN_UPDATED_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_ID);
        OffsetDateTime now = DateTimeUtils.now();
        ConnectionInfo deleted = ConnectionInfo.newBuilder().withId(connId).withDeleted(true).withUpdateTime(now).build();
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                deleted.getDeleted(),
                deleted.getUpdateTime(),
                deleted.getId()
        );
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KMCI)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }

    public void removeConnectionByDatasource(Long datasourceId) {
        Preconditions.checkNotNull(datasourceId);
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_DELETED, COLUMN_UPDATED_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_DATASOURCE_ID);
        OffsetDateTime now = DateTimeUtils.now();
        ConnectionInfo deleted = ConnectionInfo.newBuilder().withDatasourceId(datasourceId).withDeleted(true).withUpdateTime(now).build();
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                deleted.getDeleted(),
                deleted.getUpdateTime(),
                deleted.getDatasourceId()
        );
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KMCI)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }

    private static class ConnectionInfoMapper implements ResultSetMapper<ConnectionInfo> {
        public static final ResultSetMapper<ConnectionInfo> INSTANCE = new ConnectionInfoMapper();

        @Override
        public ConnectionInfo map(ResultSet rs) throws SQLException {
            return ConnectionInfo.newBuilder()
                    .withId(rs.getLong(COLUMN_ID))
                    .withDatasourceId(rs.getLong(COLUMN_DATASOURCE_ID))
                    .withName(rs.getString(COLUMN_NAME))
                    .withConnScope(ConnScope.valueOf(rs.getString(COLUMN_CONN_SCOPE)))
                    .withConnectionConfigInfo(JSONUtils.jsonToObject(rs.getString(COLUMN_CONN_CONFIG), ConnectionConfigInfo.class))
                    .withDescription(rs.getString(COLUMN_DESCRIPTION))
                    .withUpdateUser(rs.getString(COLUMN_UPDATE_USER))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(COLUMN_UPDATED_TIME)))
                    .withCreateUser(rs.getString(COLUMN_CREATE_USER))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(COLUMN_CREATE_TIME)))
                    .withDeleted(rs.getBoolean(COLUMN_DELETED))
                    .build();
        }
    }
}
