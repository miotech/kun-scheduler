package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.metadata.core.model.DatasetBaseInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.workflow.utils.JSONUtils.stringToJson;

@Component
public class DataXConfigTaskTemplateRender extends TaskTemplateRenderer{

    @DubboReference(version = "1.0")
    MetadataServiceFacade metaFacade;

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        Map<String, Object> configMap = buildTaskConfig(taskConfig, taskTemplate);

        return  TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    @Override
    public Map<String, Object> buildTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {

        String dataXTemplate = "{\n" +
                "  \"job\": {\n" +
                "    \"setting\": {\n" +
                "      \"speed\": {\n" +
                "        \"byte\": 10485760\n" +
                "      },\n" +
                "      \"errorLimit\": {\n" +
                "        \"record\": 0,\n" +
                "        \"percentage\": 0.02\n" +
                "      }\n" +
                "    },\n" +
                "    \"content\": [\n" +
                "      {\n" +
                "        \"reader\": {\n" +
                "          \"name\": \"\",\n" +
                "          \"parameter\": {\n" +
                "            \"username\": \"\",\n" +
                "            \"password\": \"\",\n" +
                "            \"column\": [\n" +
                "              \"*\"\n" +
                "            ],\n" +
                "            \"connection\": [\n" +
                "              {\n" +
                "                \"table\": [],\n" +
                "                \"jdbcUrl\": []\n" +
                "              }\n" +
                "            ],\n" +
                "            \"sliceRecordCount\": 100000\n" +
                "          }\n" +
                "        },\n" +
                "        \"writer\": {\n" +
                "          \"name\": \"\",\n" +
                "          \"parameter\": {\n" +
                "            \"username\": \"\",\n" +
                "            \"password\": \"\",\n" +
                "            \"column\": [\n" +
                "              \"*\"\n" +
                "            ],\n" +
                "            \"connection\": [\n" +
                "              {\n" +
                "                \"jdbcUrl\": \"\",\n" +
                "                \"table\": []\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";


        Long readerDataSourceId = Long.parseLong(taskConfig.get("sourceDB").toString());
        String sourcetable = taskConfig.get("sourceTable").toString();
        List<DatasetBaseInfo>  sourceDataSets = metaFacade.fetchDatasetsByDatasourceAndNameLike(readerDataSourceId, sourcetable);
        String sourceTableGid = "";
        if(!sourceDataSets.isEmpty()){
            sourceTableGid = sourceDataSets.get(0).getGid().toString();
        }


        //TODO: call API
        String readerDBType = "postgresql";
        String readerDadaXName = readerDBType.toLowerCase() + "reader";

        String readerUserName = "mock_reader_user";
        String readerPwd = "mock_reader_pwd";
        String readerDBName = "mock_reader_db";
        String readerDBHost = "mock_reader_host";
        String readerDBPort = "mock_reader_port";
        String readerJdbc = getJdbcURL(readerDBType, readerDBHost, readerDBPort, readerDBName);

        String writerDataSourceId = taskConfig.get("targetDB").toString();
        String targettable = taskConfig.get("targetTable").toString();

        String writerDBType = "postgresql";
        String writerDataXName = writerDBType.toLowerCase() + "writer";

        String targetTableGid = "mock_writer_gid";
        String writerUserName = "mock_writer_user";
        String writerPwd = "mock_writer_pwd";
        String writerDBName = "mock_writer_db";
        String writerDBHost = "mock_writer_host";
        String writerDBPort = "mock_writer_port";
        String writerJdbc = getJdbcURL(writerDBType, writerDBHost, writerDBPort, writerDBName);


        ObjectNode config = null;
        try {
            config = (ObjectNode)stringToJson(dataXTemplate);

            JsonNode content = config.get("job").get("content").get(0);
            ObjectNode reader = (ObjectNode) content.get("reader");
            reader.put("name", readerDadaXName);
            ObjectNode readerParam = (ObjectNode) reader.get("parameter");
            readerParam.put("username", readerUserName);
            readerParam.put("password", readerPwd);
            ArrayNode readerTable = (ArrayNode) readerParam.get("connection").get(0).get("table");
            readerTable.add(sourcetable);
            ArrayNode readerJDBC = (ArrayNode) readerParam.get("connection").get(0).get("jdbcUrl");
            readerJDBC.add(readerJdbc);


            ObjectNode writer = (ObjectNode)content.get("writer");
            writer.put("name", writerDataXName);
            ObjectNode writerParam = (ObjectNode) writer.get("parameter");
            writerParam.put("username", writerUserName);
            writerParam.put("password", writerPwd);
            ArrayNode writerTable = (ArrayNode) writerParam.get("connection").get(0).get("table");
            writerTable.add(targettable);
            ((ObjectNode) writerParam.get("connection").get(0)).put("jdbcUrl", writerJdbc);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("sourceTable", sourcetable);
        configMap.put("targetTable", targettable);
        configMap.put("sourceTableGid", sourceTableGid);
        configMap.put("targetTableGid", targetTableGid);
        configMap.put("jobjson", config.toString());
        return configMap;
    }

    String getJdbcURL(String dbType, String host, String port, String dbName){
        String jdbc = "";
        switch (dbType.toLowerCase()){
            case "mysql":
            case "drds":
                jdbc = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
                break;
            case "postgresql":
                jdbc = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
                break;
            case "oracle":
                jdbc = String.format("jdbc:oracle:thin:@[%s]:%s:[%s]",host, port, dbName);
                break;
            case "sqlserver":
                jdbc = String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s", host, port, dbName);
                break;
        }
        return jdbc;
    }

    private String getDBType(String dbTypeID){
        //TODO: call API
        return "postgresql";
    }

    private String getDadaSetGid(String tableName){
        //TODO: call API
        return "mock_gid";
    }

    private void getDBInfo(String dataSourceId, String host, String port, String user, String pwd, String type){
        //TODO: call API
        user = "mock_reader_user";
        pwd = "mock_reader_pwd";
        host = "mock_reader_host";
        port = "mock_reader_port";
        type = "postgresql";
    }

    private void buildDataXConfig(String dataSourceId, String dbTable) throws Exception {
        String user = "";
        String pwd = "";
        String host = "";
        String port = "";
        String typeId = "";
        getDBInfo(dataSourceId, host, port, user, pwd, typeId);
        String type = getDBType(typeId);

        String[] array = dbTable.split(".");
        if(array.length != 2)
            throw new Exception("table name invalid");
        String dbname = array[0];
        String tableName = array[1];


    }
}
