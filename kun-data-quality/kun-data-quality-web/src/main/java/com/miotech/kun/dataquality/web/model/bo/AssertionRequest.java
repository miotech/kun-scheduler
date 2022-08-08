package com.miotech.kun.dataquality.web.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssertionRequest {

    private String comparisonOperator;

    private int comparisonPeriod;

    private String expectedValue;

}
