<p align="center">
    <img src="docs/static/img/github_bg.png">
</p>
<p align="center">
    <a href="https://www.apache.org/licenses/LICENSE-2.0.txt">
        <img alt="license" src="https://img.shields.io/:license-Apache%202-blue.svg" />
    </a>
    <a href="https://github.com/miotech/kun-scheduler/actions">
        <img alt="GitHub Build" src="https://img.shields.io/github/workflow/status/miotech/kun-scheduler/ci%20ut" />
    </a>
    <a href="https://codecov.io/gh/miotech/kun-scheduler">
        <img alt="codecov" src="https://codecov.io/gh/miotech/kun-scheduler/branch/master/graph/badge.svg?token=GOFXDTB69M" />
    </a>
    <a href="https://sonarcloud.io/dashboard?id=miotech_kun-scheduler">
        <img alt="Quality Gate Status" src="https://sonarcloud.io/api/project_badges/measure?project=miotech_kun-scheduler&metric=alert_status">
    </a>
</p>

# 介绍

kun-scheduler 是一个面向大数据处理的任务调度引擎，基于”数据和元数据同样重要“的设计理念，对数据处理中的元数据会自动进行采集、汇总和分析，并呈现给用户，帮助企业和组织将数据”资产化“。适用于大规模组织、复杂数据流、多部门数据合作、高数据质量要求等企业级的数据处理场景。

kun-scheduler 的特性主要包括：

- 支持任务 DAG 的调度和运行。
- 支持多种数据源的元数据采集，包括表名，字段名，表结构，最后更新时间戳，总行数，非空比例等。
- 支持数据血缘的自动生成和展示。
- 支持 event driven 式的数据质量校验。
- 支持任务的监控告警，并支持多种渠道和多种告警规则。
- 支持多种周期的任务之间依赖。
- 支持对任务状态、运行日志的查看，对任务进行停止、重试等操作。
- 支持数据回填。

# 快速上手
## 使用 Docker

最快地试用 kun-scheduler 的方法是使用 Docker Compose 进行部署，需要满足以下要求

```
Docker >= v17.03
Docker Compose >= v1.27.0
```

部署包括以下步骤：

第1步：下载kun-scheduler

```
curl -LfO 'https://raw.githubusercontent.com/miotech/kun-scheduler/master/dist/kun-scheduler-0.7.0-rc3.tar.gz'
tar xf kun-scheduler-0.7.0-rc3.tar.gz
cd kun-scheduler
docker-compose up
```

第2步：配置hadoop环境

```
# edit hadoop.env file and config follwing params
# hive-size.xml configuration
HIVE_SITE_CONF_hive_metastore_uris=
HIVE_SITE_CONF_hive_metastore_warehouse_dir=

# hdfs-site.xml configuration
HDFS_CONF_fs_defaultFS=

# yarn-site.xml configuration
//yarn ip
YARN_CONF_yarn_resourcemanager_hostname=

# core-site.xml configuration
CORE_CONF_fs_defaultFS=
```

第3步：启动docker-compose

```
docker-compose up
```

首次启动因为需要初始化数据库等操作，所以需大约等待1分钟左右。当看到下面这行日志时，说明启动已经完成。

```
kun-app_1 ... c.miotech.kun.webapp.WebApplicationMain  : Started WebApplicationMain in 28.392 seconds
```

打开浏览器，进入`http://localhost:8080/`。

用户名和密码填入`admin/admin`，即可进入首页。

### 可能遇到的异常

如果在启动过程中 container 反复被重启以及在日志中出现以下内容：

```
...
Killed
...
```

则说明 container 所用内存超过了 docker 默认的内存 limit，需要修改 docker 配置。请参考这个[帖子](https://stackoverflow.com/questions/44417159/docker-process-killed-with-cryptic-killed-message)。

# 教程

以下会用几个简单的例子来展示 kun-scheduler 如何使用。

## 导入数据到Hive

对于csv文件，你可以通过Hive的load功能导入，例如

1. 上传cvs文件到hdfs
2. 创建Hive表
3. load csv文件到Hive表

对于MySQL中的表，你可以通过下面几种方法将其导入到Hive

1. 使用DataX导入，详细使用说明可参考: [DataX详细介绍](https://github.com/alibaba/DataX)
2. 将数据转存为csv文件后导入

## 采集 Hive 数据源中的所有表

首先我们需要添加一个 Hive 数据源。

1. 点击左侧标签页的“设置”->“数据源设置”，然后点击右上角的“新增数据源”。
2. 数据源名字可以任意填写，类型选择“Hive”，然后填写以下信息

    ```
    HiveMetaStore 地址: metaStore的uris地址
    HiveServer 主机: HiveServer的host
    HiveServer 端口: HiveServer的port
    HiveServer 用户名: HiveServer的用户名
    HiveServer 密码: HiveServer的密码
    ```

3. 添加完数据源之后，点击数据源的刷新按钮。这样 kun-scheduler 就会开始收集 Hive 中所有的表信息。
4. 由于导入过程目前还没有制作进度条，所以请耐心等待。过一段时间后数据源就会导入完成，并展示在“数据集”页面里。

## 创建一个 Spark 任务，并解析输入表和输出表

首先我们要有一个 yarn 集群，并把地址配置到 kun-scheduler 里面。

1. 点击左侧标签页的“设置”->“全局变量设置”，然后点击“创建新变量”。
2. 填写 key 为 `yarn.host`，value 则是 yarn 的地址，形如`127.0.0.1:8088`。

然后我们需要上传两个 jar 包到 HDFS 上

1. 两个 jar 包在`libs`目录下，分别名为`kun-spline-0.7.0-rc1.jar`和`spark-submit-sql-assembly-0.1.jar`。我们需要将其上传到一个能够从部署 kun-scheduler 的机器可以访问的 HDFS 地址。
2. 和上面相同的，需要在“全局变量设置”中增加以下配置

    ```
    lineage.analysis.jar  # kun-spline-0.7.0.rc1.jar的hdfs地址，例如 hdfs://hadoopNS/libs/kun-spline-0.7.0-rc1.jar
    spark.sql.jar # spark-submit-sql-assembly-0.1.jar的hdfs地址，例如 hdfs://hadoopNS/libs/spark-submit-sql-assembly-0.1.jar
    lineage.output.path # 用于存放血缘解析结果的临时目录，例如 hdfs://hadoopNS/var/kun/
    ```

接下来我们开始创建 SparkSQL 任务。

1. 点击左侧标签页的“数据开发”，选择右上角的“创建任务”，创建“SparkSQL”任务。
2. 输入任务名之后，任务会被创建，点击进入任务的配置页面，填写sql脚本

    ```
    DROP TABLE IF EXISTS kun.sales_record;
    CREATE TABLE kun.sales_record (id BIGINT, name STRING,price DECIMAL, date STRING);
    INSERT INTO TABLE kun.sales_record VALUES
        (1, 'cola', 3,'2021-07-01'),
        (2, 'ice-cream', 5,'2021-07-01'),
        (3, 'cola', 3,'2021-07-02');
    ```

3. 配置完任务参数后，再打开“调度规则”，选择“手动触发”，保存。
4. 点击右上角的“试运行”按钮，可以对任务进行试跑。
5. 如试跑无问题，点击右上角的“发布”按钮，可以将任务发布到线上，也就会正式开始调度了。不过由于我们选择的调度方式是”手动触发“，所以不会被自动调度，但可以手动补数据。
6. 仿照之前的步骤，再创建一个spark-sql任务，填写sql脚本

    ```
    CREATE TABLE if not exists kun.daily_sales;
    INSERT INTO kun.daily_sales_record SELECT date,sum(price) from kun.sales_record group by date;
    ```

并在"调度规则"的"上游配置"中填入之前创建的任务名并选中搜索结果，最后将这个任务保存并发布。
7. 再次回到“数据开发”页面，勾选中刚刚创建的任务，点击右上角的“执行数据回填”，就会触发一次补数据。补数据的结果可以在左侧标签页的“补数据实例”处看到。

最后我们再来看下数据血缘的解析结果。

1. 打开左侧标签页的“数据探索”，点击“数据集”。
2. 搜索 `sales_record` 表或 `daily_sales` 表，在血缘中可以看到刚刚创建的任务。

## 构造一条 Pipeline

当有了上面这个任务之后，我们再来创建一个任务流。

首先和上面步骤相同的，再创建一个新任务，然后打开“调度规则”->“上游配置”。目前支持两种方式依赖上游任务

1. 输入数据集
2. 上游搜索

其中“输入数据集”是声明当前任务使用了哪些表，这样调度系统就会自动把产出到这些表的任务作为当前任务的上游任务。而“上游搜索“则是通过任务名称来搜索上游任务。

当创建完任务间依赖之后，调度系统会根据依赖关系，按顺序调度这两个任务。
