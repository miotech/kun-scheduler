package com.miotech.kun.metadata.databuilder.extract.stat;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class StatExtractorTemplate implements DatasetStatExtractor {

    public abstract DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) throws SQLException, ClassNotFoundException;

    @Override
    public Dataset extract(Dataset dataset, DataSource dataSource) throws Exception {
        DataWarehouseStatTemplate statTemplate = null;
        try {
            Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());
            statTemplate = buildDataWarehouseStatTemplate(dataset, dataSource);
            resultBuilder.withDatasetStat(DatasetStat.newBuilder()
                    .withRowCount(statTemplate.getRowCount(dataSource.getType()))
                    .withStatDate(LocalDateTime.now())
                    .withLastUpdatedTime(getLastUpdatedTime(dataset, dataSource))
                    .build());

            final DataWarehouseStatTemplate finalStatTemplate = statTemplate;
            List<DatasetFieldStat> fieldStats = dataset.getFields().stream().map(field ->
                    finalStatTemplate.getFieldStats(field, dataSource.getType())).collect(Collectors.toList());
            return resultBuilder.withFieldStats(fieldStats).build();
        } finally {
            if (statTemplate != null) {
                statTemplate.close();
            }
        }
    }

    public LocalDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        return null;
    }
}
