package com.miotech.kun.security.authenticate.resolver;

import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public interface AttributesResolver {

    String getUsernameKey();

    String resolveUsername(Map userInfoMap);
}
