# 数据监控

数据监控模块会对以下模块进行监控：
- 数据发现
- 数据开发

#### 数据发现
数据发现监控主要针对以下指标：
- 失败的测试用例的统计信息
- 数据集的统计信息

通过失败测试用例列表，我们能够快速跳转到相应的测试用例详细页面，这样能够方便我们去尽早定位数据质量的问题。

数据集的统计信息能够帮助我们了解可能发生数据异常的数据集。

#### 数据开发
数据开发监控主要针对以下指标：
- 任务的统计信息

通过对数据开发任务的监控，我们能够纵览任务运行的全局概况，包括所处各个阶段的任务数量、任务的状态以及失败任务的错误信息等等。

------------

### 项目介绍
本模块基于SpringBoot框架开发，对外提供标准的RESTful API接口。项目的根目录位于*KUN/kun-data-dashboard*，其中配置了gradle作为构建工具，依赖构建详情可参考*build.gradle*文件。

本地的构建步骤如下：
```bash
cd KUN/
./gradlew kun-data-dashboard:bootJar
cd kun-data-dashboard/
docker build -t kun-data-dashboard:master -f Dockerfile .
```

本项目依赖以下的组件：
- PostgreSQL数据库
- 调度系统(kun-workflow)
- 安全中心(kun-security)
