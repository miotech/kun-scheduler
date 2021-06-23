<p align="center">
    <img style="transform: scale(0.8)" src="docs/static/img/github_bg.png">
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
Docer >= v17.03
Docker Compose >= v1.27.0
```

部署包括以下步骤：

```
curl -LfO 'https://raw.githubusercontent.com/miotech/kun-scheduler/master/dist/kun-scheduler-0.7.0-rc1.tar.gz'
tar xf kun-scheduler-0.7.0-rc1.tar.gz
cd kun-scheduler
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

## 采集 Hive 数据源中的所有表

首先我们需要添加一个 Hive 数据源。（注意：目前仅支持 mysql 的 metastore）

1. 点击左侧标签页的“设置”->“数据源设置”，然后点击右上角的“新增数据源”。
2. 数据源名字可以任意填写，类型选择“Hive”，然后填写以下信息

    ```
    dataSettings.field.metastoreHost: metastore的数据库host
    dataSettings.field.metastorePort: metastore的数据库port
    dataSettings.field.metastoreDatabaseName: metastore使用的database name
    dataSettings.field.metastoreUsername: metastore的数据库的username
    dataSettings.field.metastorePassword: metastore的数据库的password
    dataSettings.field.datastoreHost: HiveServer的host
    dataSettings.field.datastorePort: HiveServer的port
    dataSettings.field.datastoreUsername: HiveServer的用户名
    dataSettings.field.datastorePassword: HiveServer的密码
    ```
3. 添加完数据源之后，点击数据源的刷新按钮。这样 kun-scheduler 就会开始收集 Hive 中所有的表信息。
4. 由于导入过程目前还没有制作进度条，所以请耐心等待。过一段时间后数据源就会导入完成，并展示在“数据集”页面里。

## 创建一个 Spark 任务，并解析输入表和输出表

首先我们要有一个 livy 集群，并把地址配置到 kun-scheduler 里面。

1. 点击左侧标签页的“设置”->“全局变量设置”，然后点击“创建新变量”。
2. 填写 key 为 `livy.host`，value 则是 livy 的地址，形如`http://10.0.2.14:8998`。

然后我们需要上传血缘解析的 jar 包到 HDFS 上。

1. 血缘解析的 jar 包在安装包的`libs`目录下，名为`kun-spline-0.7.0-rc1.jar`。我们需要将其上传到一个能够从部署 kun-scheduler 的机器可以访问的 HDFS 地址。
2. 和上面相同的，需要在“全局变量设置”中增加以下配置

    ```
    lineage.analysis.jar: 血缘解析 jar 包上传以后的地址
    lineage.output.path: HDFS 的临时目录，因为血缘解析结果会暂存为 HDFS 上的文件
    s3.access.key: 如使用 s3 作为存储，则需要在这里增加 s3 的 access key
    s3.secret.key: 同上，s3 的 secret access key
    ```

接下来我们开始创建 Spark 任务。

1. 点击左侧标签页的“数据开发”，选择右上角的“创建任务”，创建“Spark”任务。
2. 输入任务名之后，任务会被创建，点击进入任务的配置页面，有以下参数

    ```
    application files: Spark 脚本所在的 jar 包路径。需要注意的是 jar 包必须上传到 HDFS 或 s3 上面，这里填写的是 HDFS 或 s3 路径。必填。
    application class name: Spark 脚本的 main class name。
    application jars: 任务运行所需的其他额外 jar 包。选填。
    application args: 任务执行时的命令行参数。选填。
    ```
3. 配置完任务参数后，再打开“调度规则”，选择“手动触发”，保存。
4. 点击右上角的“试运行”按钮，可以对任务进行试跑。
5. 如试跑无问题，点击右上角的“发布”按钮，可以将任务发布到线上，也就会正式开始调度了。不过由于我们选择的调度方式是”手动触发“，所以不会被自动调度，但可以手动补数据。
6. 再次回到“数据开发”页面，勾选中刚刚创建的任务，点击右上角的“执行数据回填”，就会触发一次补数据。补数据的结果可以在左侧标签页的“补数据实例”处看到。

最后我们再来看下数据血缘的解析结果。假设上面这个任务读取了 `dwd.transaction_details`，并写入 `dws.transaction_summary` 表。我们可以在“数据集”中看到血缘解析的结果。

1. 打开左侧标签页的“数据探索”，点击“数据集”。
2. 搜索 `transaction_details` 表或 `transaction_summary` 表，在血缘中可以看到刚刚创建的任务。

## 构造一条 Pipeline

当有了上面这个任务之后，我们再来创建一个任务流。

首先和上面步骤相同的，再创建一个新任务，然后打开“调度规则”->“上游配置”。目前支持两种方式依赖上游任务

1. 输入数据集
2. 上游搜索

其中“输入数据集”是声明当前任务使用了哪些表，这样调度系统就会自动把产出到这些表的任务作为当前任务的上游任务。而“上游搜索“则是通过任务名称来搜索上游任务。

当创建完任务间依赖之后，调度系统会根据依赖关系，按顺序调度这两个任务。
