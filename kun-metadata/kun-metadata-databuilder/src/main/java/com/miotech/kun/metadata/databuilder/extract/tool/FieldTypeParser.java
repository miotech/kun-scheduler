package com.miotech.kun.metadata.databuilder.extract.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldTypeParser {

    private static final String ARRAY = "ARRAY";

    private FieldTypeParser() {
    }

    public static List<DatasetField> parse(JsonNode root, String parent) {
        List<DatasetField> fieldList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String keyName = parent == null ? fieldName : String.format("%s.%s", parent, fieldName);
            JsonNode node = root.get(fieldName);
            if (node.isObject()) {
                fieldList.addAll(parse(node, keyName));
            } else if (node.isArray()) {
                if (node.isEmpty()) {
                    fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.Type.valueOf(ARRAY), ARRAY), ""));
                    continue;
                }

                JsonNode firstElement = node.get(0);
                if (firstElement instanceof ObjectNode) {
                    fieldList.addAll(parse(firstElement, keyName));
                } else {
                    fieldList.add(new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.Type.valueOf(ARRAY), ARRAY), ""));
                }
            } else {
                String fieldType = null;
                if (node.isNull()) {
                    fieldType = "UNKNOWN";
                } else {
                    if (node.isNumber())
                        fieldType = "NUMBER";
                    else if (node.isTextual() || node.isBinary())
                        fieldType = "CHARACTER";
                    else if (node.isBoolean())
                        fieldType = "BOOLEAN";
                }
                DatasetField field = new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.Type.valueOf(fieldType), fieldType), "");
                fieldList.add(field);
            }
        }
        return fieldList;
    }

}
