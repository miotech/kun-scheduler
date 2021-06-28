<p align="center">
    <img style="transform: scale(0.8)" src="https://raw.githubusercontent.com/miotech/KUN/master/docs/static/img/github_bg.png">
</p>

<div align="center">
    <p>(If you are Chinese user, please read <a href="https://github.com/miotech/KUN/blob/master/README_zh_cn.md">中文文档</a>)</p>
</div>

# Intro

*Kun-scheduler* is a task scheduler specific to data engineering which takes care of both data and metadata. It can collect, organize, analyze and monitor various metadata (including storage, table, schema, data lineage, data usage, etc) for you, and unleash the full power of your Cloud-Native Data Lake.

*Kun-scheduler* aims as follows:

- Schedule and execute complex data pipelines and DAGs.
- Collect various datasource's metadata, including table name, column name, schema, last updated timestamp, row count, non-null ratio, etc.
- Generate data lineage by inspecting data tasks and pipelines.
- Event-driven data sanity check.
- Monitor task status and alert on task failure.
- Support data backfilling.

# Get Started
## Running kun-scheduler in Docker

The fastest way to start *kun-scheduler* is using Docker Compose. Make sure you have Docker and Docker Compose installed, and they meet the minimum required version.

```
Docer >= v17.03
Docker Compose >= v1.27.0
```

Then you can start *kun-scheduler* with following commands

```
curl -LfO 'https://raw.githubusercontent.com/miotech/kun-scheduler/master/dist/kun-scheduler-0.7.0-rc1.tar.gz'
tar xf kun-scheduler-0.7.0-rc1.tar.gz
cd kun-scheduler
docker-compose up
```

The bootstrap of *kun-scheduler* is a bit slow so please wait some minutes patiently. You should see below message when the bootstrap is complete.

```
kun-app_1 ... c.miotech.kun.webapp.WebApplicationMain  : Started WebApplicationMain in 28.392 seconds
```

Now you can access *kun-scheduler* web console via the url `http://localhost:8080/`. You may use the default username `admin` and password `admin` to login.

### Trouble Shooting

If you found a mysterious error message `Killed` in bootstrap, it is caused by the memory limit of docker, you need to change the configuration. Please refer to [this post](https://stackoverflow.com/questions/44417159/docker-process-killed-with-cryptic-killed-message).

# Tutorial

Coming soon...
