package com.miotech.kun.datadashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class DataDashboardBeanConfig {

    @Value("${workflow.base-url:http://kun-workflow:8088}")
    String workflowUrl;
}
