package com.miotech.kun.dataquality.web.model.bo;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/8/11
 */
@Data
public class SearchLogRequest {

    Long startTime;

    Long endTime;

    boolean export;
}
