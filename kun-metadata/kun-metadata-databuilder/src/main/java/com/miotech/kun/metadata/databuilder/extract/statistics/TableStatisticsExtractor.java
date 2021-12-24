package com.miotech.kun.metadata.databuilder.extract.statistics;

import java.time.OffsetDateTime;

public interface TableStatisticsExtractor {

    default OffsetDateTime getLastUpdatedTime() {
        return null;
    }

    default Long getRowCount() {
        return null;
    }

    default Long getTotalByteSize() {
        return null;
    }

}
