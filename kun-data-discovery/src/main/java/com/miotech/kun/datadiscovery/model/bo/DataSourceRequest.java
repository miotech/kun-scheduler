package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.util.JSONUtils;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DataSourceRequest {

    private String datasourceType;

    private String name;

    private JSONObject information;

    private List<String> tags;

    private String createUser;

    private Long createTime;

    private String updateUser;

    private Long updateTime;

    public com.miotech.kun.metadata.core.model.vo.DataSourceRequest convert() {
        return com.miotech.kun.metadata.core.model.vo.DataSourceRequest .newBuilder()
                .withDatasourceType(DatasourceType.valueOf(datasourceType))
                .withName(name)
                .withConnectionConfig(JSONUtils.jsonToObject(information, ConnectionConfig.class))
                .withTags(tags)
                .withCreateUser(createUser)
                .withUpdateUser(updateUser)
                .build();
    }

}
