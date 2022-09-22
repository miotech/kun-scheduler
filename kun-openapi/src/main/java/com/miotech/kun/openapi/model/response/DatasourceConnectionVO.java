package com.miotech.kun.openapi.model.response;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.openapi.model.response.ConnectionInfoVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 15:25
 **/
@Data
@NoArgsConstructor
public class DatasourceConnectionVO {

    private List<ConnectionInfoVO> userConnectionList;

    public DatasourceConnectionVO(List<ConnectionInfo> userConnectionList) {
        List<ConnectionInfoVO> connectionInfos = userConnectionList.stream().map(ConnectionInfoVO::new).collect(Collectors.toList());
        Map<ConnScope, List<ConnectionInfoVO>> map = connectionInfos.stream().collect(Collectors.groupingBy(ConnectionInfoVO::getConnScope));
        this.userConnectionList = Lists.newArrayList(map.get(ConnScope.USER_CONN));

    }

}
