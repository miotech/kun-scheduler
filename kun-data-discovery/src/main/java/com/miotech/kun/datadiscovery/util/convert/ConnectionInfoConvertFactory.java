package com.miotech.kun.datadiscovery.util.convert;

import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoSecurityVO;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoVO;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.vo.ConnectionBasicInfoVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-02-09 09:56
 **/
public class ConnectionInfoConvertFactory implements ConverterFactory<ConnectionInfo, ConnectionInfoVO> {

    @Override
    public <T extends ConnectionInfoVO> Converter<ConnectionInfo, T> getConverter(Class<T> targetType) {
        return source -> {
            ConnectionInfoVO vo = createConnectionInfoVO(targetType);
            vo.setId(source.getId());
            vo.setDatasourceId(source.getDatasourceId());
            vo.setName(source.getName());
            vo.setConnScope(source.getConnScope());
            vo.setConnectionConfigInfo(source.getConnectionConfigInfo());
            vo.setCreateUser(source.getCreateUser());
            vo.setCreateTime(source.getCreateTime());
            vo.setUpdateUser(source.getUpdateUser());
            vo.setUpdateTime(source.getUpdateTime());
            vo.setDeleted(source.getDeleted());
            return (T) vo;
        };
    }

    private <T extends ConnectionInfoVO> ConnectionInfoVO createConnectionInfoVO(Class<T> targetType) {
        if (targetType.equals(ConnectionInfoSecurityVO.class)) {
            return new ConnectionInfoSecurityVO();
        }
        return new ConnectionInfoVO();
    }
}
