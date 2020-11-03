---
slug: /quick-start-zh
title: 快速开始
---

# 快速开始

此章节将指导用户以最方便快捷的方式去启动整个KUN项目 

### 0 准备
**Docker**  -  v18.0 或更高版本
**Docker-Compose** -  v1.0 或更高版本

### 1 拉取项目
``` bash
git clone https://github.com/miotech/KUN.git
cd KUN
```

### 2 获取最新镜像
最新的release版本镜像已经在dockerhub上可用,修改`.env`文件即可使用:
```
IMAGE_REMOTE_ORG=miotechoss/ 
```
如果你想使用本地镜像来替代，可以使用各个模块下的*Dockerfile*自行build:
#####  kun-data-discovery

```bash
./gradlew kun-data-discovery:bootJar
cd kun-data-discovery
docker build -t kun-data-discovery:master -f Dockerfile .
```

#####  kun-data-dashboard
```bash
./gradlew kun-data-dashboard:bootJar
cd kun-data-dashboard
docker build -t kun-data-dashboard:master -f Dockerfile .
```
#####  kun-data-platform
```bash
./gradlew kun-data-platform:kun-data-platform-web:bootJar
cd kun-data-platform
docker build -t kun-data-platform:master -f Dockerfile .
```
#####  kun-frontend
```bash
./gradlew kun-frontend:yarnBuild
cd kun-frontend
docker build -t kun-frontend:master -f Dockerfile .
```
#####  kun-metadata
```bash
./gradlew kun-metadata:shadowJar
cd kun-data-metadata
docker build -t kun-metadata:master -f Dockerfile .
```
#####  kun-workflow
```bash
./gradlew kun-workflow:shadowJar
cd kun-workflow
docker build -t kun-workflow:master -f Dockerfile .
```
#####  kun-security
```bash
./gradlew kun-security:kun-security-server:bootJar
cd kun-security
docker build -t kun-security:master -f Dockerfile .
```

### 3 启动
使用docker-compose去启动整个项目
```bash
docker-compose up -d
```
对于任何定制化的启动，可以通过修改*docker-compose.yml*文件来进行

### 4 验证
使用下方的初始化账户访问 http://localhost:9801:
 - username: admin
 - password: admin
 
登陆成功后即可开始体验KUN的所有功能
