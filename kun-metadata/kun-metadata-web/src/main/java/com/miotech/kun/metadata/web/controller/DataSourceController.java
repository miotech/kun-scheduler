package com.miotech.kun.metadata.web.controller;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DataSourceBasicInfo;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoVo;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import com.miotech.kun.metadata.web.model.vo.AcknowledgementVO;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class DataSourceController {

    @Inject
    DataSourceService dataSourceService;

    @RouteMapping(url = "/datasources/_search", method = "POST")
    public PaginationVO<DataSource> getDataSourceSearch(@RequestBody DataSourceSearchFilter filter) {
        return dataSourceService.fetchDataSources(filter.getPageNum(), filter.getPageSize(), filter.getName());
    }

    @RouteMapping(url = "/datasources/list", method = "GET")
    public List<DataSource> getDataSourceList() {
        return dataSourceService.fetchDataSources();
    }

    @RouteMapping(url = "/datasources/_search/basic", method = "POST")
    public PaginationVO<DataSourceBasicInfoVo> getDataSourceBasic(@RequestBody DataSourceSearchFilter filter) {
        List<DataSourceBasicInfo> dataSourceBasicInfoList = dataSourceService.fetchWithFilter(filter);
        Integer count = dataSourceService.fetchTotalCountWithFilter(filter);
        if (count <= 0) {
            return new PaginationVO<>(filter.getPageSize(), filter.getPageNum(), count, Lists.newArrayList());
        }
        List<DataSourceBasicInfoVo> collect = dataSourceBasicInfoList.stream().map(this::convertVo).collect(Collectors.toList());
        return new PaginationVO<>(filter.getPageSize(), filter.getPageNum(), count, collect);
    }

    private DataSourceBasicInfoVo convertVo(DataSourceBasicInfo dataSourceBasicInfo) {
        return DataSourceBasicInfoVo.newBuilder()
                .withId(dataSourceBasicInfo.getId())
                .withDatasourceConfigInfo(dataSourceBasicInfo.getDatasourceConfigInfo())
                .withName(dataSourceBasicInfo.getName())
                .withTags(dataSourceBasicInfo.getTags())
                .withDatasourceType(dataSourceBasicInfo.getDatasourceType())
                .withCreateUser(dataSourceBasicInfo.getCreateUser())
                .withCreateTime(dataSourceBasicInfo.getCreateTime())
                .withUpdateTime(dataSourceBasicInfo.getUpdateTime())
                .withUpdateUser(dataSourceBasicInfo.getUpdateUser())
                .build();
    }


    @RouteMapping(url = "/datasource", method = "POST")
    public DataSourceBasicInfoVo createDataSource(@RequestBody DataSourceBasicInfoRequest dataSourceBasicInfoRequest) {
        return convertVo(dataSourceService.create(dataSourceBasicInfoRequest));
    }

    @RouteMapping(url = "/datasource/{id}", method = "PUT")
    public DataSourceBasicInfoVo updateDataSource(@RouteVariable Long id,
                                                  @RequestBody DataSourceBasicInfoRequest dataSourceBasicInfoRequest) {
        return convertVo(dataSourceService.update(id, dataSourceBasicInfoRequest));
    }

    @RouteMapping(url = "/datasource/{id}", method = "DELETE")
    public AcknowledgementVO deleteDataSource(@RouteVariable Long id) {
        dataSourceService.delete(id);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url = "/datasource/{id}", method = "GET")
    public DataSource getDataSource(@RouteVariable Long id) {
        return dataSourceService.fetchDatasource(id).orElse(null);
    }

}
