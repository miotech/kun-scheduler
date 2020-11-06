---
slug: /module-kun-data-discovery-zh
title: 安全中心
---

安全中心模块包括以下几个子模块：
- 用户信息管理
- 认证
- 授权

#### 用户信息管理
对已认证的用户录入数据库并对外提供相应的RESTful API接口

#### 认证
目前提供基于JSON的认证作为默认的认证方式，用户可以修改`kun-security:kun-security-server`模块下的`src/main/resources/kun-users.json`文件来添加或删除用户。

同时也提供了基于spring security的自定义接口，让用户能够自己实现认证：
1. 在`kun-security:kun-security-server`模块下的`com.miotech.kun.security.authenticate`包下创建自定义类并实现`org.springframework.security.authentication.AuthenticationProvider`接口
2. 在自定义类上添加注解`@KunAuthenticateProvider`（用户可参考示例类`com.miotech.kun.security.authenticate.CustomAuthenticateProvider`）
3. 修改`kun-security:kun-security-server`模块下的`src/main/resources/application-local.yaml`配置文件：
    ```
    security:
      auth:
        type: CUSTOM
    ```
   或者修改环境变量SECURITY_AUTH_TYPE=CUSTOM

#### 授权
目前授权功能暂不支持自定义扩展，将在未来的开发计划中支持

------------

### 项目介绍
本模块基于SpringBoot框架开发，对外提供标准的RESTful API接口。项目的根目录位于*KUN/kun-security*，其中配置了gradle作为构建工具，依赖构建详情可参考*build.gradle*文件。

本地的构建步骤如下：
```bash
cd KUN/
./gradlew kun-security:kun-security-server:bootJar
cd kun-security/
docker build -t kun-security:master -f Dockerfile .
```

本项目依赖以下的组件：
- PostgreSQL数据库
