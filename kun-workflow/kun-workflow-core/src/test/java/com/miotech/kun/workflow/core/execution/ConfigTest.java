package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;





public class ConfigTest {


    @Test
    public void deserializeConfig(){
        String json = "{\n" +
                "        \"livyHost\":\"http://10.0.1.198:899\",\n" +
                "        \"dataStoreUrl\":\"s3://com.miotech.data.prd/Database/DM\",\n" +
                "        \"sparkSQL\":\"create table dm.source_cbirc_punishment_manual as select * from dm.source_cbirc_punishment_manual_tmp;\"\n" +
                "    }";
        Config expectConfig = Config.newBuilder()
                .addConfig("livyHost","http://10.0.1.198:899")
                .addConfig("dataStoreUrl","s3://com.miotech.data.prd/Database/DM")
                .addConfig("sparkSQL","create table dm.source_cbirc_punishment_manual as select * from dm.source_cbirc_punishment_manual_tmp;")
                .build();
        Config config = JSONUtils.jsonToObject(json,Config.class);
        assertThat(config,is(expectConfig));

    }

    @Test
    public void serializeConfig(){
        String expectJson = "{\"livyHost\": \"http://10.0.1.198:899\", \"dataStoreUrl\": \"s3://com.miotech.data.prd/Database/DM\", \"sparkSQL\": \"create table dm.source_cbirc_punishment_manual as select * from dm.source_cbirc_punishment_manual_tmp;\"}";
        Config config = Config.newBuilder()
                .addConfig("livyHost","http://10.0.1.198:899")
                .addConfig("dataStoreUrl","s3://com.miotech.data.prd/Database/DM")
                .addConfig("sparkSQL","create table dm.source_cbirc_punishment_manual as select * from dm.source_cbirc_punishment_manual_tmp;")
                .build();
        String json = JSONUtils.toJsonString(config);
        JSONAssert.assertEquals(expectJson,json,true);
    }
}
