package com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.ElasticSearchClient;
import com.miotech.kun.metadata.databuilder.exception.ElasticSearchServiceUnavailableException;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import com.miotech.kun.workflow.core.model.lineage.ElasticSearchIndexStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ElasticSearchIndexSchemaExtractor extends SchemaExtractorTemplate {

    private final String index;
    private final ElasticSearchDataSource elasticSearchDataSource;
    private final ElasticSearchClient elasticSearchClient;
    private final HttpClientUtil httpClientUtil;

    @Inject
    public ElasticSearchIndexSchemaExtractor(ElasticSearchDataSource elasticSearchDataSource, String index) {
        super(elasticSearchDataSource.getId());
        this.index = index;
        this.elasticSearchDataSource = elasticSearchDataSource;
        this.elasticSearchClient = new ElasticSearchClient(elasticSearchDataSource);
        this.httpClientUtil = Guice.createInjector().getInstance(HttpClientUtil.class);
    }

    @Override
    public List<DatasetField> getSchema() {
        try {
            String endpoint = getEndPoint(getEsVersion());
            Request request = new Request("POST", endpoint);
            request.setJsonEntity(String.format("{\"query\":\"describe \\\"%s\\\"\"}", index));

            Response response = elasticSearchClient.performRequest(request);
            String json = EntityUtils.toString(response.getEntity());
            JsonNode root = JSONUtils.stringToJson(json);

            if (root.get("rows").isEmpty()) {
                return Lists.newArrayList();
            }

            return parseDatasetFieldFromJson(root);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public DataStore getDataStore() {
        return new ElasticSearchIndexStore(elasticSearchDataSource.getUrl(), index);
    }

    @Override
    public String getName() {
        return index;
    }

    @Override
    protected void close() {
        elasticSearchClient.close();
    }

    private String getEndPoint(String version) {
        if (version.charAt(0) >= '7') {
            return "_sql";
        } else {
            return "_xpack/sql";
        }
    }

    private String getEsVersion() throws JsonProcessingException {
        String version = parseVersion(httpClientUtil.doGet("http://" + elasticSearchDataSource.getUrl()));
        if (StringUtils.isBlank(version)) {
            throw new ElasticSearchServiceUnavailableException("get es version error");
        }

        return version;
    }

    private String parseVersion(String result) throws JsonProcessingException {
        Preconditions.checkState(StringUtils.isNotBlank(result), "call es, response empty");
        JsonNode root = JSONUtils.stringToJson(result);
        if (root == null || root.isEmpty()) {
            return null;
        }

        JsonNode version = root.get("version");
        if (version == null || !version.isObject()) {
            return null;
        }

        return version.get("number").textValue();
    }

    private List<DatasetField> parseDatasetFieldFromJson(JsonNode root) {
        List<DatasetField> datasetFields = Lists.newArrayList();
        for (final JsonNode node : root.get("rows")) {
            Iterator<JsonNode> it = node.iterator();
            String name = it.next().asText();
            if (name.endsWith("keyword"))
                continue;
            String type = it.next().asText();
            if (type.equals("STRUCT"))
                continue;
            DatasetFieldType.Type fieldType;
            switch (type) {
                case "VARCHAR":
                case "VARBINARY":
                    fieldType = DatasetFieldType.Type.CHARACTER;
                    break;
                case "REAL":
                case "TINYINT":
                case "SMALLINT":
                case "INTEGER":
                case "BIGINT":
                case "DOUBLE":
                case "FLOAT":
                    fieldType = DatasetFieldType.Type.NUMBER;
                    break;
                case "BOOLEAN":
                    fieldType = DatasetFieldType.Type.BOOLEAN;
                    break;
                case "TIMESTAMP":
                    fieldType = DatasetFieldType.Type.DATETIME;
                    break;
                default:
                    fieldType = DatasetFieldType.Type.UNKNOW;
            }
            datasetFields.add(new DatasetField(name, new DatasetFieldType(fieldType, type), ""));
        }

        return datasetFields;
    }

}