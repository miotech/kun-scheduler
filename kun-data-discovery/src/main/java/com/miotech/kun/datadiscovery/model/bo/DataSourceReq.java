package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoSecurityVO;
import com.miotech.kun.datadiscovery.model.vo.DatasourceConnectionVO;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DataSourceReq {

    private DatasourceType datasourceType;

    private String name;

    private DatasourceConnectionVO datasourceConnection;

    private Map<String, Object> datasourceConfigInfo;

    private List<String> tags;

    private String createUser;

    private OffsetDateTime createTime;

    private String updateUser;

    private OffsetDateTime updateTime;


}
