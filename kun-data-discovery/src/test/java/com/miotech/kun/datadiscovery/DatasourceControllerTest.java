package com.miotech.kun.datadiscovery;

import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.bo.DatabaseRequest;
import com.miotech.kun.datadiscovery.model.entity.Datasource;
import com.miotech.kun.datadiscovery.model.entity.DatasourceType;
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
public class DatasourceControllerTest extends BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testGetDatasourceType() throws Exception {
        RequestResult<List<DatasourceType>> result = getDatasourceType();
        Assert.assertEquals(5, result.getResult().size());
    }

    private RequestResult<List<DatasourceType>> getDatasourceType() throws Exception {
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl("/metadata/database/types")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<List<DatasourceType>>>() {
        }.getType();
        return JSONUtils.toJavaObject(jsonResult, type);
    }

    @Test
    public void testAddDatasource() throws Exception {
        List<DatasourceType> datasourceTypes = getDatasourceType().getResult();
        String dsName = "DB-TEST-1";
        DatabaseRequest databaseRequest = new DatabaseRequest();
        databaseRequest.setName(dsName);
        databaseRequest.setTypeId(datasourceTypes.get(0).getId());
        databaseRequest.setInformation(new JSONObject());
        databaseRequest.setCreateUser(DEFAULT_USER);
        databaseRequest.setCreateTime(DEFAULT_TIME);
        databaseRequest.setUpdateUser(DEFAULT_USER);
        databaseRequest.setUpdateTime(DEFAULT_TIME);
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .post(getUrl("/metadata/database/add"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(databaseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<Datasource>>() {
        }.getType();
        RequestResult<Datasource> result = JSONUtils.toJavaObject(jsonResult, type);
        Assert.assertEquals(dsName, result.getResult().getName());
    }
}
