package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.datasource.DataSourceType;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import com.miotech.kun.metadata.web.model.vo.AcknowledgementVO;

import java.util.List;

@Singleton
public class DataSourceController {

    @Inject
    DataSourceService dataSourceService;

    @RouteMapping(url = "/datasources/_search", method = "POST")
    public PaginationVO<DataSource> getDataSource(@RequestBody DataSourceSearchFilter filter) {
        return dataSourceService.fetchDataSources(filter.getPageNum(), filter.getPageSize(), filter.getName());
    }

    @RouteMapping(url = "/datasource", method = "POST")
    public DataSource createDataSource(@RequestBody DataSourceRequest dataSourceRequest) {
        return dataSourceService.create(dataSourceRequest);
    }

    @RouteMapping(url = "/datasource/{id}", method = "PUT")
    public DataSource updateDataSource(@RouteVariable Long id,
                                       @RequestBody DataSourceRequest dataSourceRequest) {
        return dataSourceService.update(id, dataSourceRequest);
    }

    @RouteMapping(url = "/datasource/{id}", method = "DELETE")
    public AcknowledgementVO deleteDataSource(@RouteVariable Long id) {
        dataSourceService.delete(id);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url = "/datasource/types", method = "GET")
    public List<DataSourceType> getDataSourceTypes() {
        return dataSourceService.getAllTypes();
    }

}
