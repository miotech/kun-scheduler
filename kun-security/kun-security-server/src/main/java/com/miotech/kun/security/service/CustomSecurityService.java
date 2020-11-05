package com.miotech.kun.security.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2020/9/1
 */
@Service
@ConditionalOnProperty(name = "security.auth.type", havingValue = "CUSTOM")
public class CustomSecurityService extends AbstractSecurityService {
}
