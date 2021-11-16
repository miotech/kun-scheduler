package com.miotech.kun.datadiscovery;

import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.entity.DataSourceVO;
import com.miotech.kun.datadiscovery.model.entity.DataSourceTemplateVO;
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
public class DataSourceVOControllerTest extends BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testGetDatasourceType() throws Exception {
        RequestResult<List<DataSourceTemplateVO>> result = getDatasourceType();
        Assert.assertEquals(5, result.getResult().size());
    }

    private RequestResult<List<DataSourceTemplateVO>> getDatasourceType() throws Exception {
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .get(getUrl("/metadata/datasource/types")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<List<DataSourceTemplateVO>>>() {
        }.getType();
        return JSONUtils.toJavaObject(jsonResult, type);
    }

    @Test
    public void testAddDatasource() throws Exception {
        List<DataSourceTemplateVO> dataSourceTemplateVOs = getDatasourceType().getResult();
        String dsName = "DB-TEST-1";
        com.miotech.kun.datadiscovery.model.bo.DataSourceVo dataSourceVo = new com.miotech.kun.datadiscovery.model.bo.DataSourceVo();
        dataSourceVo.setName(dsName);
        dataSourceVo.setDatasourceType(dataSourceTemplateVOs.get(0).getType());
        dataSourceVo.setInformation(new JSONObject());
        dataSourceVo.setCreateUser(DEFAULT_USER);
        dataSourceVo.setCreateTime(DEFAULT_TIME);
        dataSourceVo.setUpdateUser(DEFAULT_USER);
        dataSourceVo.setUpdateTime(DEFAULT_TIME);
        String jsonResult = mockMvc.perform(MockMvcRequestBuilders
                .post(getUrl("/metadata/datasource/add"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(dataSourceVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        Type type = new TypeToken<RequestResult<DataSourceVO>>() {
        }.getType();
        RequestResult<DataSourceVO> result = JSONUtils.toJavaObject(jsonResult, type);
        Assert.assertEquals(dsName, result.getResult().getName());
    }
}
