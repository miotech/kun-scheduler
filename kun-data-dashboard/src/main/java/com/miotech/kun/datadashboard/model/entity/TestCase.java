package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class TestCase {

    String result;

    String errorReason;

    Long updateTime;

    Long continuousFailingCount;

    String caseOwner;
}
