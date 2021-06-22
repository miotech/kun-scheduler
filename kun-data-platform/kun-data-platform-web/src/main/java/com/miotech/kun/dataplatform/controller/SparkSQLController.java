package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.sparksql.service.SparkThriftServerClient;
import com.miotech.kun.dataplatform.common.sparksql.vo.SparkSQLExecuteRequest;
import com.miotech.kun.dataplatform.common.sparksql.vo.SparkSQLExecuteResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/spark-sql")
@Api(tags = "SparkSQL")
public class SparkSQLController {

    @Autowired
    private SparkThriftServerClient client;

    @PostMapping("/_execute")
    public RequestResult<SparkSQLExecuteResponse> execute(@RequestBody SparkSQLExecuteRequest executeRequest) {
        return RequestResult.success(client.execute(executeRequest));
    }

}
