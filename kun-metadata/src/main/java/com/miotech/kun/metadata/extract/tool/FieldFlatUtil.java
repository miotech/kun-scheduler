package com.miotech.kun.metadata.extract.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldFlatUtil {

    public static List<DatasetField> flatFields(JsonNode root, String parent) {
        List<DatasetField> fieldList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String keyName = parent == null ? fieldName : String.format("%s.%s", parent, fieldName);
            JsonNode node = root.get(fieldName);
            if (node.isObject()) {
                fieldList.addAll(flatFields(node, keyName));
            } else if (node.isArray()) {
                if (node.isEmpty()) {
                    fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.convertRawType("ARRAY"), "ARRAY"), ""));
                }
                for (JsonNode n : node) {
                    if (n instanceof ObjectNode) {
                        fieldList.addAll(flatFields(n, keyName));
                    } else {
                        fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.convertRawType("ARRAY"), "ARRAY"), ""));
                    }
                    break;
                }
            } else {
                String fieldType = null;
                if (node.isNull()) {
                    fieldType = "UNKNOW";
                } else {
                    if (node.isNumber())
                        fieldType = "NUMBER";
                    else if (node.isTextual() || node.isBinary())
                        fieldType = "STRING";
                    else if (node.isBoolean())
                        fieldType = "BOOL";
                }
                DatasetField field = new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.convertRawType(fieldType), fieldType), "");
                fieldList.add(field);
            }
        }
        return fieldList;
    }

}
