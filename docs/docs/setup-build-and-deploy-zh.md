---
slug: /setup-build-and-deploy-zh
title: 构建部署
---

此章节将指导用户来构建部署KUN。

如果你想自行构建本地镜像，可以使用各个模块下的*Dockerfile*自行构建:
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

### 3 运行
使用docker或者docker-compose去部署整个项目。
```bash
docker-compose up -d
```
对于任何定制化的部署，可以通过修改*docker-compose.yml*文件来进行。

### 4 验证
使用下方的初始化账户访问 http://localhost:9801:
 - username: admin
 - password: admin
 
登陆成功后即可开始体验KUN的所有功能。

