package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.workflow.utils.JSONUtils;
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

    public com.miotech.kun.metadata.core.model.datasource.DataSourceRequest convert() {
        return com.miotech.kun.metadata.core.model.datasource.DataSourceRequest.newBuilder()
                .withTypeId(typeId)
                .withName(name)
                .withConnectionInfo(JSONUtils.jsonStringToMap(JSONUtils.toJsonString(information)))
                .withTags(tags)
                .withCreateUser(createUser)
                .withUpdateUser(updateUser)
                .build();
    }

}
