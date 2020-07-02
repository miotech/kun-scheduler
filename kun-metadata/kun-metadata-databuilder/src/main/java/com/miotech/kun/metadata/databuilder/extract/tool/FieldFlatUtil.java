package com.miotech.kun.metadata.databuilder.extract.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldFlatUtil {

    private static final String ARRAY = "ARRAY";

    private FieldFlatUtil() {
    }

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
                    fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.convertRawType(ARRAY), ARRAY), ""));
                    continue;
                }

                JsonNode firstElement = node.get(0);
                if (firstElement instanceof ObjectNode) {
                    fieldList.addAll(flatFields(firstElement, keyName));
                } else {
                    fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.convertRawType(ARRAY), ARRAY), ""));
                }
            } else {
                String fieldType = null;
                if (node.isNull()) {
                    fieldType = "UNKNOWN";
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
