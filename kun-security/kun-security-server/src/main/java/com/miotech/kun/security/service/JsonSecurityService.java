package com.miotech.kun.security.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "security.auth.type", havingValue = "JSON", matchIfMissing = true)
public class JsonSecurityService extends AbstractSecurityService {
}