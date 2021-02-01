package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.FieldStatistics;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.util.List;
import java.util.stream.Collectors;

public abstract class StatisticsExtractorTemplate implements DatasetStatisticsExtractor {

    public abstract DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource);

    @Override
    public Long getRowCount(Dataset dataset, DataSource dataSource) {
        DataWarehouseStatTemplate statTemplate = null;
        try {
            statTemplate = buildDataWarehouseStatTemplate(dataset, dataSource);
            return statTemplate.getRowCount(dataSource.getType());

        } finally {
            if (statTemplate != null) {
                statTemplate.close();
            }
        }
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics(Dataset dataset, DataSource dataSource) {
        DataWarehouseStatTemplate statTemplate = null;
        try {
            statTemplate = buildDataWarehouseStatTemplate(dataset, dataSource);

            final DataWarehouseStatTemplate finalStatTemplate = statTemplate;
            return dataset.getFields().stream().map(field ->
                    finalStatTemplate.getFieldStats(field, dataSource.getType())).collect(Collectors.toList());
        } finally {
            if (statTemplate != null) {
                statTemplate.close();
            }
        }
    }

}
