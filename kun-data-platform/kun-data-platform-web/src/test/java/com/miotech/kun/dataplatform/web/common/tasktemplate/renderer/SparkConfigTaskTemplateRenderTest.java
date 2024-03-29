package com.miotech.kun.dataplatform.web.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.operator.SparkConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SparkConfigTaskTemplateRenderTest {

    private TaskTemplateRenderer taskTemplateRenderer = new SparkConfigTaskTemplateRender();

    @Test
    public void render_with_datasource() {
        List<ParameterDefinition> sourceParams = Collections.singletonList(
                ParameterDefinition.newBuilder()
                        .withName("sourceDataSource")
                        .withType("datasource")
                        .build()
        );
        TaskTemplate templateWithDatasource = initSparkTemplate(sourceParams);
        TaskConfig taskConfig = taskTemplateRenderer.render(ImmutableMap.of("sourceDataSource", "1"),
                templateWithDatasource,
                TaskDefinition.newBuilder().withName("sparkJobName").build());
        assertThat(taskConfig.getParams().get("sparkConf"),
                is("{\"spark.driver.extraJavaOptions\":\" -Dkun.dataplatform.datasource.1=${dataplatform.datasource.1} -Dkun.dataplatform=eyJzb3VyY2VEYXRhU291cmNlIjoiMSJ9\"}"));
        assertThat((String) taskConfig.getParams().get(SparkConfiguration.CONF_LIVY_BATCH_NAME), is("sparkJobName"));
    }

    private TaskTemplate initSparkTemplate(List<ParameterDefinition> parameters) {
        ConfigKey configKey = new ConfigKey();
        configKey.setName("sparkConf");
        configKey.setType(ConfigDef.Type.STRING);
        Operator operator = Operator.newBuilder()
                .withConfigDef(Collections.singletonList(
                        configKey
                ))
                .build();

        return TaskTemplate.newBuilder()
                .withDisplayParameters(parameters)
                .withOperator(operator)
                .withDefaultValues(ImmutableMap.of("sparkConf", "{}"))
                .build();
    }
}