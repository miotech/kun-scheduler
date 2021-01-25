package com.miotech.kun.dataplatform.common.backfill.vo;

import com.miotech.kun.commons.db.sql.SortOrder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BackfillSearchParams {
    private String name;
    private List<Long> creators;
    private Integer pageNumber;
    private Integer pageSize;
    private OffsetDateTime timeRngStart;
    private OffsetDateTime timeRngEnd;
    private String sortKey;
    private SortOrder sortOrder;
}
