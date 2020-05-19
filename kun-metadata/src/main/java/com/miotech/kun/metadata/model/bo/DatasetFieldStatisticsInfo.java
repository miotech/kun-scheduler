package com.miotech.kun.metadata.model.bo;

import java.math.BigDecimal;
import java.util.Date;

public class DatasetFieldStatisticsInfo {

    private Long fieldId;

    private Date statsDate;

    private long distinctCount;

    private long nonnullCount;

    private BigDecimal nonnullPercentage;

}
