package com.miotech.kun.datadashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class DataDashboardBeanConfig {

    @Value("${infra.base-url:http://kun-infra:8088}")
    String workflowUrl;
}
