package com.miotech.kun.dataquality.web.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SQLParseResult {

    List<String> relatedDatasetNames;

    List<String> columnNames;

}
