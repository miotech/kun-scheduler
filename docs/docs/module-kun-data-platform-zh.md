---
slug: /module-kun-data-platform-zh
title: 数据开发
---
数据开发模块包括以下几个子模块：
- 数据开发
- 运维中心

#### 数据开发

数据开发模块主要包括一个供用户开发数据任务的环境。用户可以在界面上编写处理逻辑，编辑调度周期和任务间依赖，试运行调试任务代码，并在开发完成后发布上线。

目前Kun内置有以下几种Operator供用户使用：

- SparkOperator
- SparkSQLOperator
- DataxOperator

后续也会支持用户开发并上传自己的Operator。

#### 运维中心

运维中心负责提供用户一个搜索、查看、运维、管理已发布任务每天运行的实例的环境。

运维中心展示以下任务实例：

- 周期实例
- 补数据实例

------------

### 项目介绍

本模块基于SpringBoot框架开发，对外提供标准的RESTful API接口。项目的根目录位于*KUN/kun-data-platform/kun-data-platform-web*，其中配置了gradle作为构建工具，依赖构建详情可参考*build.gradle*文件。

本地的构建步骤如下：

```bash
cd KUN/
./gradlew kun-data-platform:bootJar
cd kun-data-platform/
docker build -t kun-data-platform:master -f Dockerfile .
```

本项目依赖以下的组件：
- PostgreSQL数据库
- 元数据服务(kun-metadata)
- 调度系统(kun-workflow)
- 安全中心(kun-security)
