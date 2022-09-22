package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSourceVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String datasourceType;

    private String name;


    private DatasourceConnectionVO datasourceConnection;


    private Map<String, Object> datasourceConfigInfo;

    private String createUser;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createTime;

    private String updateUser;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updateTime;

    private List<String> tags;
}
