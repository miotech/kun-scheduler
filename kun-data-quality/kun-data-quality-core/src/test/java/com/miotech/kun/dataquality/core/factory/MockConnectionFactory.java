package com.miotech.kun.dataquality.core.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-16 10:29
 **/
public class MockConnectionFactory {
    public static ConnectionInfo createConnectionInfo(
            String name,
            ConnScope connScope,
            ConnectionConfigInfo connectionConfigInfo
    ) {
        String user = "test_user";
        return ConnectionInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withConnScope(connScope)
                .withName(name)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withCreateUser(user)
                .withUpdateUser(user)
                .withDeleted(false)
                .build();
    }

    public static DatasourceConnection createDatasourceConnection(ConnectionConfigInfo commConfig) {
        ConnectionInfo user_connection = MockConnectionFactory.createConnectionInfo("user_connection", ConnScope.USER_CONN, commConfig);
        ConnectionInfo data_connection = MockConnectionFactory.createConnectionInfo("data_connection", ConnScope.DATA_CONN, commConfig);
        ConnectionInfo meta_connection = MockConnectionFactory.createConnectionInfo("meta_connection", ConnScope.METADATA_CONN, commConfig);
        ConnectionInfo storage_connection = MockConnectionFactory.createConnectionInfo("storage_connection", ConnScope.STORAGE_CONN, commConfig);
        return DatasourceConnection
                .newBuilder()
                .withUserConnectionList(Lists.newArrayList(user_connection))
                .withDataConnection(data_connection)
                .withMetadataConnection(meta_connection)
                .withStorageConnection(storage_connection)
                .build();
    }


}
