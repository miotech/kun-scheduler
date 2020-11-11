---
slug: /
title: 构建部署
---

KUN is an all-in-one data management platform. This tutorial aims at guiding user and developer to setup the whole KUN project in local with minimum efforts.

KUN embeds gradle as the build tool for the whole project. For involved compiling sub-modules, please refer to the *settings.gradle* file.

### 0 Prerequisite
**Docker**  -  v18.0 or above

**Docker-Compose** -  v1.0 or above

### 1 Pull Repository
``` bash
git clone https://github.com/miotech/KUN.git
cd KUN
```

### 2 Retrieve Latest Image
We have built our latest image and push to dockerhub with public access. If you want to pull the image from dockerhub, just change `.env` file with the following:
```
IMAGE_REMOTE_ORG=miotechoss/ 
```
Alternatively, you can build image on your local by embedded *Dockerfile* in each following module:

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

### 3 Up and Run
Use docker-compose to run the whole application with initialization. For any specific module setup or modification, please refer to the *docker-compose.yml* file.
```bash
docker-compose up -d
```

### 4 Verification
Go and visit http://localhost:9801 with the following credentiails:
 - username: admin
 - password: admin
 
If you login succesfully, it will work like a charm.
