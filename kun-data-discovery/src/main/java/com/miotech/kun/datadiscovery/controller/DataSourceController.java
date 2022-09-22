package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceReq;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.vo.*;
import com.miotech.kun.datadiscovery.service.DataSourceAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
@RequiredArgsConstructor
public class DataSourceController {


    private final DataSourceAppService dataSourceAppService;

    @GetMapping("/metadata/datasources/search")
    public RequestResult<DataSourceBasicPage> searchDataSource(BasicSearchRequest basicSearchRequest) {
        return RequestResult.success(dataSourceAppService.search(basicSearchRequest));
    }

    @Deprecated
    @GetMapping("/metadata/datasources")
    public RequestResult<DataSourcePage> getDataSource(DataSourceSearchRequest dataSourceSearchRequest) {
        return RequestResult.success(dataSourceAppService.search(dataSourceSearchRequest));
    }

    @Deprecated
    @GetMapping("/metadata/datasources/list")
    public RequestResult<List<DataSourceVO>> getDataSource() {
        return RequestResult.success(dataSourceAppService.list());
    }

    @PostMapping("/metadata/datasource/add")
    public RequestResult<DataSourceVO> addDataSource(@RequestBody DataSourceReq dataSourceReq) {
        return RequestResult.success(dataSourceAppService.add(dataSourceReq));
    }

    @PostMapping("/metadata/datasource/{id}/update")
    public RequestResult<DataSourceVO> updateDataSource(@PathVariable Long id,
                                                        @RequestBody DataSourceReq dataSourceReq) {
        return RequestResult.success(dataSourceAppService.update(id, dataSourceReq));
    }

    @DeleteMapping("/metadata/datasource/{id}")
    public RequestResult<IdVO> deleteDataSource(@PathVariable Long id) {
        dataSourceAppService.delete(id);
        IdVO idVO = new IdVO();
        idVO.setId(id);
        return RequestResult.success(idVO);
    }

    @PostMapping("/metadata/datasource/{id}/pull")
    public RequestResult<PullProcessVO> pullDataSource(@PathVariable Long id) {
        PullProcessVO vo = dataSourceAppService.pullDataSource(id);
        return RequestResult.success(vo);
    }

    @GetMapping("/metadata/datasource/processes/latest")
    public RequestResult<Map<String, PullProcessVO>> pullDataset(@RequestParam List<Long> dataSourceIds) {
        Map<String, PullProcessVO> map = dataSourceAppService.fetchLatestPullProcessByDataSourceIds(dataSourceIds);
        return RequestResult.success(map);
    }


    @GetMapping("/metadata/datasource/{id}")
    public RequestResult<DataSourceVO> findDataSource(@PathVariable Long id) {
        return RequestResult.success(dataSourceAppService.findDataSource(id));
    }
    @GetMapping("/metadata/datasource/user-connection/{id}")
    public RequestResult<List<ConnectionInfoSecurityVO>> findDataSourceUserConnection(@PathVariable Long id) {
        return RequestResult.success(dataSourceAppService.findDataSourceUserConnection(id));
    }

}
