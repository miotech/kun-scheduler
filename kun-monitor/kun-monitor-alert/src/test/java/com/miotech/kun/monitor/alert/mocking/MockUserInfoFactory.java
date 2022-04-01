package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.UserInfo;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

public class MockUserInfoFactory {

    private MockUserInfoFactory() {
    }

    public static UserInfo create() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(IdGenerator.getInstance().nextId());
        userInfo.setUsername("admin");
        userInfo.setPassword("admin");
        userInfo.setPermissions(ImmutableSet.of());
        userInfo.setAuthOriginInfo(null);
        userInfo.setFirstName("admin");
        userInfo.setLastName("admin");
        userInfo.setEmail("test@163.com");
        userInfo.setWeComId("1");
        userInfo.setUserGroupId(1L);
        return userInfo;
    }

}
