package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.util.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;
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

    private Long typeId;

    private String name;

    private JSONObject information;

    private List<String> tags;

    private String createUser;

    private Long createTime;

    private String updateUser;

    private Long updateTime;

    public com.miotech.kun.metadata.core.model.vo.DataSourceRequest convert() {
        return com.miotech.kun.metadata.core.model.vo.DataSourceRequest.newBuilder()
                .withTypeId(typeId)
                .withName(name)
                .withConnectionInfo(new ConnectionInfo(JSONUtils.objToMap(information, String.class, Object.class)))
                .withTags(tags)
                .withCreateUser(createUser)
                .withUpdateUser(updateUser)
                .build();
    }

}
