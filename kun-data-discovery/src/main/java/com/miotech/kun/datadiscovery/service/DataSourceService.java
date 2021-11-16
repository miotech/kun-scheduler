package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.util.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.DatasourceTemplate;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import com.miotech.kun.security.service.BaseSecurityService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class DataSourceService extends BaseSecurityService {

    @Autowired
    MetadataService metadataService;

    public DataSourceBasicPage search(BasicSearchRequest basicSearchRequest) {
        List<com.miotech.kun.metadata.core.model.datasource.DataSource> records = metadataService.search(basicSearchRequest.getKeyword(), basicSearchRequest.getPageNumber(), basicSearchRequest.getPageSize()).getRecords();
        return new DataSourceBasicPage(records.stream()
                .map(ds -> new DatasourceBasic(ds.getId(), ds.getName()))
                .collect(Collectors.toList()));
    }

    public DataSourcePage search(DataSourceSearchRequest datasourceSearchRequest) {
        PaginationVO<com.miotech.kun.metadata.core.model.datasource.DataSource> result = metadataService.search(datasourceSearchRequest.getSearch(),
                datasourceSearchRequest.getPageNumber(), datasourceSearchRequest.getPageSize());
        List<com.miotech.kun.metadata.core.model.datasource.DataSource> records = result.getRecords();
        DataSourcePage dataSourcePage = new DataSourcePage(records.stream()
                .map(ds -> DataSourceVO.builder()
                        .id(ds.getId())
                        .datasourceType(ds.getDatasourceType().name())
                        .typeId(ds.getTypeId())
                        .name(ds.getName())
                        .connectionConfig(JSONUtils.jsonToObject(ds.getConnectionConfig(), JSONObject.class))
                        .createUser(ds.getCreateUser())
                        .createTime(ds.getCreateTime())
                        .updateUser(ds.getUpdateUser())
                        .updateTime(ds.getUpdateTime())
                        .tags(ds.getTags())
                        .build())
                .collect(Collectors.toList()));
        dataSourcePage.setTotalCount(result.getTotalCount());
        dataSourcePage.setPageNumber(datasourceSearchRequest.getPageNumber());
        dataSourcePage.setPageSize(datasourceSearchRequest.getPageSize());

        return dataSourcePage;
    }

    public List<DataSourceTemplateVO> getAllTypes() {
        List<DatasourceTemplate> types = metadataService.getTypes();
        return types.stream()
                .map(type -> new DataSourceTemplateVO(type.getType(),type.getId()))
                .collect(Collectors.toList());
    }

    public DataSourceVO add(com.miotech.kun.datadiscovery.model.bo.DataSourceVo dataSourceVo) {
        fillCreateRequest(dataSourceVo);
        DataSource ds = metadataService.create(dataSourceVo.convert());
        return DataSourceVO.builder()
                .id(ds.getId())
                .datasourceType(ds.getDatasourceType().name())
                .name(ds.getName())
                .connectionConfig(JSONUtils.jsonToObject(ds.getConnectionConfig(), JSONObject.class))
                .createUser(ds.getCreateUser())
                .createTime(ds.getCreateTime())
                .updateUser(ds.getUpdateUser())
                .updateTime(ds.getUpdateTime())
                .tags(ds.getTags())
                .build();
    }

    public DataSourceVO update(Long id, com.miotech.kun.datadiscovery.model.bo.DataSourceVo dataSourceVo) {
        fillUpdateRequest(dataSourceVo);
        DataSource ds = metadataService.update(id, dataSourceVo.convert());
        return DataSourceVO.builder()
                .id(ds.getId())
                .datasourceType(ds.getDatasourceType().name())
                .name(ds.getName())
                .connectionConfig(JSONUtils.jsonToObject(ds.getConnectionConfig(), JSONObject.class))
                .createUser(ds.getCreateUser())
                .createTime(ds.getCreateTime())
                .updateUser(ds.getUpdateUser())
                .updateTime(ds.getUpdateTime())
                .tags(ds.getTags())
                .build();
    }

    public void delete(Long id) {
        metadataService.delete(id);
    }

    private void fillCreateRequest(com.miotech.kun.datadiscovery.model.bo.DataSourceVo dataSourceVo) {
        String username = getCurrentUsername();
        dataSourceVo.setCreateUser(username);
        dataSourceVo.setUpdateUser(username);
        dataSourceVo.setCreateTime(System.currentTimeMillis());
        dataSourceVo.setUpdateTime(dataSourceVo.getCreateTime());
    }

    private void fillUpdateRequest(com.miotech.kun.datadiscovery.model.bo.DataSourceVo dataSourceVo) {
        String username = getCurrentUsername();
        dataSourceVo.setUpdateUser(username);
        dataSourceVo.setUpdateTime(dataSourceVo.getCreateTime());
    }

}
