package com.miotech.kun.metadata.extract.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.model.DatasetField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldFlatUtil {

    public static List<DatasetField> flatFields(JsonNode root, String parent) {
        List<DatasetField> fieldList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while(fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String keyName = parent == null ? fieldName : String.format("%s.%s", parent, fieldName);
            JsonNode node = root.get(fieldName);
            if (node.isObject()) {
                fieldList.addAll(flatFields(node, keyName));
            } else if (node.isArray()) {
                if (node.isEmpty()) {
                    fieldList.add(new DatasetField(keyName, "ARRAY", ""));
                }
                for(JsonNode n : node){
                    fieldList.addAll(flatFields(n, keyName));
                }
            } else {
                String fieldType = null;
                if (node.isNull()){
                    fieldType = "UNKNOW";
                }
                else {
                    if (node.isNumber())
                        fieldType = "NUMBER";
                    else if (node.isTextual() || node.isBinary())
                        fieldType = "STRING";
                    else if (node.isBoolean())
                        fieldType = "BOOL";
                }
                DatasetField field = new DatasetField(keyName, fieldType, "");
                fieldList.add(field);
            }
        }
        return fieldList;
    }

}
