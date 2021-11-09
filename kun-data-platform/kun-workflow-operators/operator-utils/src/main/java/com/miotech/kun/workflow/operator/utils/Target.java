package com.miotech.kun.workflow.operator.utils;

import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

public class Target{

    private String schema;
    private String name;

    public Target(String name_, String schema_){
        schema = schema_;
        name = name_;
    }

    public Target(){
        schema = "";
        name = "";
    }

    public static Target fromExecuteTarget(ExecuteTarget target){
        if(target == null){
            return new Target();
        }
        String name = target.getName();
        String schema = (String) target.getProperty("schema");
        return new Target(name, schema);
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

}
