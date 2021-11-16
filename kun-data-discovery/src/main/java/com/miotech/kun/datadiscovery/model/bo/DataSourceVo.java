package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.util.JSONUtils;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
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
public class DataSourceVo {

    private String datasourceType;

    private String name;

    private JSONObject information;

    private List<String> tags;

    private String createUser;

    private Long createTime;

    private String updateUser;

    private Long updateTime;

    public DataSourceRequest convert() {
        return DataSourceRequest.newBuilder()
                .withDatasourceType(DatasourceType.valueOf(datasourceType))
                .withName(name)
                .withConnectionConfig(JSONUtils.jsonToObject(information, ConnectionConfig.class))
                .withTags(tags)
                .withCreateUser(createUser)
                .withUpdateUser(updateUser)
                .build();
    }

}
