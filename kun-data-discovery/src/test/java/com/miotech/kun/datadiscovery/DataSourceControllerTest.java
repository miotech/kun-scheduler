package com.miotech.kun.datadiscovery;

import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.DataSourceRequest;
import com.miotech.kun.datadiscovery.model.entity.DataSource;
import com.miotech.kun.datadiscovery.model.entity.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Type;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author: Jie Chen
 * @created: 2020/6/12
 */
@Slf4j
public class DataSourceControllerTest extends BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testGetDatasourceType() throws Exception {
        RequestResult<List<DataSourceType>> result = getDatasourceType();
        Assert.assertEquals(5, result.getResult().size());
    }

    private RequestResult<List<DataSourceType>> getDatasourceType() throws Exception {
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl("/metadata/datasource/types")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<List<DataSourceType>>>() {
        }.getType();
        return JSONUtils.toJavaObject(jsonResult, type);
    }

    @Test
    public void testAddDatasource() throws Exception {
        List<DataSourceType> dataSourceTypes = getDatasourceType().getResult();
        String dsName = "DB-TEST-1";
        DataSourceRequest dataSourceRequest = new DataSourceRequest();
        dataSourceRequest.setName(dsName);
        dataSourceRequest.setTypeId(dataSourceTypes.get(0).getId());
        dataSourceRequest.setInformation(new JSONObject());
        dataSourceRequest.setCreateUser(DEFAULT_USER);
        dataSourceRequest.setCreateTime(DEFAULT_TIME);
        dataSourceRequest.setUpdateUser(DEFAULT_USER);
        dataSourceRequest.setUpdateTime(DEFAULT_TIME);
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .post(getUrl("/metadata/datasource/add"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(dataSourceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<DataSource>>() {
        }.getType();
        RequestResult<DataSource> result = JSONUtils.toJavaObject(jsonResult, type);
        Assert.assertEquals(dsName, result.getResult().getName());
    }
}
