package com.miotech.kun.workflow.core.task;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class TaskTest {

    @Test
    public void deserializeTask() throws IOException {
        String taskString = "{\n" +
                "                \"id\": \"128007739946303488\",\n" +
                "                \"name\": \"`PROD 数据源: TWSE上市公司基本信息`: 计算: dm.twse_source_company_basic_info\",\n" +
                "                \"description\": \"Deployed Data Platform Task : 125067517759590400\",\n" +
                "                \"operatorId\": \"76671693556285440\",\n" +
                "                \"config\": {\n" +
                "                    \"values\": {\n" +
                "                        \"sql\": \"select\\n    *\\nfrom\\n    dm.crawler_twse_company_basic_info\",\n" +
                "                        \"args\": \"-e dev -m TASK_CENTER -s GENERAL_SQL_STEP\",\n" +
                "                        \"jars\": \"\",\n" +
                "                        \"files\": \"${ dataplatform.etl.jar }\",\n" +
                "                        \"limit\": \"-1\",\n" +
                "                        \"livyHost\": \"${ dataplatform.livy.host }\",\n" +
                "                        \"sparkConf\": \"{\\\"spark.driver.extraJavaOptions\\\":\\\" -Dkun.dataplatform=eyJzcWwiOiJzZWxlY3RcbiAgICAqXG5mcm9tXG4gICAgZG0uY3Jhd2xlcl90d3NlX2NvbXBhbnlfYmFzaWNfaW5mbyIsInRhcmdldFRhYmxlTmFtZSI6ImRtLnR3c2Vfc291cmNlX2NvbXBhbnlfYmFzaWNfaW5mbyIsImluc2VydE1vZGUiOiJvdmVyd3JpdGUiLCJsaW1pdCI6Ii0xIn0=\\\"}\",\n" +
                "                        \"variables\": \"{}\",\n" +
                "                        \"writeMode\": \"overwrite\",\n" +
                "                        \"application\": \"com.miotech.etl.knowledge_graph.app.Application\",\n" +
                "                        \"targetTableName\": \"dm.twse_source_company_basic_info\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"scheduleConf\": {\n" +
                "                    \"type\": \"SCHEDULED\",\n" +
                "                    \"cronExpr\": \"0 40 5 * * ?\"\n" +
                "                },\n" +
                "                \"dependencies\": [\n" +
                "                    {\n" +
                "                        \"upstreamTaskId\": \"128007344507322368\",\n" +
                "                        \"downstreamTaskId\": \"128007739946303488\",\n" +
                "                        \"dependencyFunc\": \"latestTaskRun\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tags\": [\n" +
                "                    {\n" +
                "                        \"key\": \"owner\",\n" +
                "                        \"value\": \"105960795535310848\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"isArchived\",\n" +
                "                        \"value\": \"false\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"taskTemplateName\",\n" +
                "                        \"value\": \"MioSparkSQL\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"project\",\n" +
                "                        \"value\": \"KUN_DATA_PLATFORM\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"commitId\",\n" +
                "                        \"value\": \"128007738142752768\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"env\",\n" +
                "                        \"value\": \"PROD\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"type\",\n" +
                "                        \"value\": \"scheduled\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"key\": \"definitionId\",\n" +
                "                        \"value\": \"125067517759590400\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }";
        ObjectMapper objectMapper = new ObjectMapper();
        Task task = objectMapper.readValue(taskString, Task.class);
        assertThat(task.getId(),is(128007739946303488l));
        assertThat(task.getDependencies(),hasSize(1));
        TaskDependency taskDependency = task.getDependencies().get(0);
        assertThat(taskDependency.getUpstreamTaskId(),is(128007344507322368l));
        assertThat(taskDependency.getDownstreamTaskId(),is(128007739946303488l));
    }

}
