package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.persistence.DataSourceRepository;
import com.miotech.kun.datadiscovery.util.JSONUtils;
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
    DataSourceRepository datasourceRepository;

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
                .map(ds -> DataSource.builder()
                        .id(ds.getId())
                        .typeId(ds.getTypeId())
                        .name(ds.getName())
                        .connectInfo(JSONUtils.jsonToObject(ds.getConnectionInfo().getValues(), JSONObject.class))
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

    public List<DataSourceType> getAllTypes() {
        List<com.miotech.kun.metadata.core.model.datasource.DataSourceType> types = metadataService.getTypes();
        return types.stream()
                .map(type -> new DataSourceType(type.getId(), type.getName(), type.getFields().stream()
                        .map(field -> DatasourceTypeField.builder()
                                .name(field.getName())
                                .sequenceOrder(field.getSequenceOrder())
                                .format(field.getFormat())
                                .require(field.isRequire())
                                .build())
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public DataSource add(DataSourceRequest dataSourceRequest) {
        fillCreateRequest(dataSourceRequest);
        com.miotech.kun.metadata.core.model.datasource.DataSource ds = metadataService.create(dataSourceRequest.convert());
        return DataSource.builder()
                .id(ds.getId())
                .typeId(ds.getTypeId())
                .name(ds.getName())
                .connectInfo(JSONUtils.jsonToObject(ds.getConnectionInfo().getValues(), JSONObject.class))
                .createUser(ds.getCreateUser())
                .createTime(ds.getCreateTime())
                .updateUser(ds.getUpdateUser())
                .updateTime(ds.getUpdateTime())
                .tags(ds.getTags())
                .build();
    }

    public DataSource update(Long id, DataSourceRequest dataSourceRequest) {
        fillUpdateRequest(dataSourceRequest);
        com.miotech.kun.metadata.core.model.datasource.DataSource ds = metadataService.update(id, dataSourceRequest.convert());
        return DataSource.builder()
                .id(ds.getId())
                .typeId(ds.getTypeId())
                .name(ds.getName())
                .connectInfo(JSONUtils.jsonToObject(ds.getConnectionInfo().getValues(), JSONObject.class))
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

    private void fillCreateRequest(DataSourceRequest dataSourceRequest) {
        String username = getCurrentUsername();
        dataSourceRequest.setCreateUser(username);
        dataSourceRequest.setUpdateUser(username);
        dataSourceRequest.setCreateTime(System.currentTimeMillis());
        dataSourceRequest.setUpdateTime(dataSourceRequest.getCreateTime());
    }

    private void fillUpdateRequest(DataSourceRequest dataSourceRequest) {
        String username = getCurrentUsername();
        dataSourceRequest.setUpdateUser(username);
        dataSourceRequest.setUpdateTime(dataSourceRequest.getCreateTime());
    }

}
