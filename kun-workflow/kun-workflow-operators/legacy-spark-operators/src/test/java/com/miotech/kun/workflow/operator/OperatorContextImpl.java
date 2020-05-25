package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OperatorContextImpl implements OperatorContext {

    private Map<String, String> params = new HashMap();
    private Map<String, String> vars = new HashMap();

    public OperatorContextImpl(){
        params.put("files", "s3://com.miotech.data.prd/Project/miotech/knowledge-graph-etl/release/com/miotech/knowledge-graph-etl/1.0/knowledge-graph-etl-1.0.jar");
        params.put("application", "com.miotech.etl.knowledge_graph.app.Application");
        params.put("args", "-e dev -m TASK_CENTER -s $STEP -i 51257436958359552");
        params.put("name", "test_operator_3");
        params.put("LIVY_HOST", "http://<livy_ip>:8998");
        params.put("SPARK_MASTER", "<livy_ip>");
        params.put("SPARK_PORT", "8088");
        vars.put("STEP", "CHOICE_DM_FULLY_UPDATE_STEP");

    }

    public String getParameter(String name){
        return params.getOrDefault(name, "");
    }

    public String getVariable(String name){
        return vars.getOrDefault(name, "");
    }

    public Logger getLogger() {
        return (Logger) LoggerFactory.getLogger(OperatorContextImpl.class);
    }


}
