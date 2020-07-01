package com.miotech.kun.metadata.databuilder.extract.impl.configurable;

import com.amazonaws.services.glue.model.Column;
import com.amazonaws.services.glue.model.Table;
import com.beust.jcommander.internal.Lists;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.model.*;
import com.miotech.kun.metadata.databuilder.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.DatasetNameGenerator;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GlueTableExtractor extends ExtractorTemplate {
    private static final Logger logger = LoggerFactory.getLogger(GlueTableExtractor.class);

    private final Table table;
    private final ConfigurableDataSource dataSource;

    public GlueTableExtractor(ConfigurableDataSource dataSource, Table table) {
        super(dataSource.getId());
        this.table = table;
        this.dataSource = dataSource;
    }

    @Override
    protected List<DatasetField> getSchema() {
        List<DatasetField> fields = Lists.newArrayList();
        if (table.getStorageDescriptor() == null || CollectionUtils.isEmpty(table.getStorageDescriptor().getColumns())) {
            return fields;
        }

        for (Column column : table.getStorageDescriptor().getColumns()) {
            DatasetField.Builder datasetFieldBuilder = DatasetField.newBuilder();
            datasetFieldBuilder.withName(column.getName())
                    .withComment(column.getComment())
                    .withFieldType(new DatasetFieldType(convertRawType(column.getType()), column.getType()));
            fields.add(datasetFieldBuilder.build());
        }
        return fields;
    }

    @Override
    protected DatasetFieldStat getFieldStats(DatasetField datasetField) {
        JDBCStatService statService = new JDBCStatService(table.getDatabaseName(), table.getName(),
                QueryEngine.parseDatabaseTypeFromDataSource(dataSource.getQueryEngine()));
        return statService.getFieldStats(datasetField, dataSource.getQueryEngine());
    }

    @Override
    protected DatasetStat getTableStats() {
        JDBCStatService statService = new JDBCStatService(table.getDatabaseName(), table.getName(),
                QueryEngine.parseDatabaseTypeFromDataSource(dataSource.getQueryEngine()));
        return statService.getTableStats(dataSource.getQueryEngine());
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(QueryEngine.parseConnInfos(dataSource.getQueryEngine())[0], table.getDatabaseName(), table.getName());
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.HIVE, table.getName());
    }

    private DatasetFieldType.Type convertRawType(String rawType) {
        if ("string".equals(rawType) ||
                rawType.startsWith("varchar") ||
                rawType.startsWith("char")) {
            return DatasetFieldType.Type.CHARACTER;
        } else if ("timestamp".equals(rawType) ||
                "date".equals(rawType)) {
            return DatasetFieldType.Type.DATETIME;
        } else if (rawType.startsWith("array")) {
            return DatasetFieldType.Type.ARRAY;
        } else if (rawType.startsWith("decimal") ||
                "double".equals(rawType) ||
                "number".equals(rawType) ||
                "int".equals(rawType) ||
                "bigint".equals(rawType)) {
            return DatasetFieldType.Type.NUMBER;
        } else if (rawType.startsWith("struct")) {
            return DatasetFieldType.Type.STRUCT;
        } else if ("boolean".equals(rawType) || "BOOL".equals(rawType)) {
            return DatasetFieldType.Type.BOOLEAN;
        } else {
            logger.warn("unknown type: {}", rawType);
            return DatasetFieldType.Type.UNKNOW;
        }
    }
}
