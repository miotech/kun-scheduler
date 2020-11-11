---
slug: /setup-installation-zh
title: 快速安装
---

此章节将指导用户快速在本地安装运行KUN。

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

### 3 运行
使用docker-compose去运行整个项目。
```bash
docker-compose up -d
```
对于任何定制化的运行，可以通过修改*docker-compose.yml*文件来进行。

### 4 验证
使用下方的初始化账户访问 http://localhost:9801:
 - username: admin
 - password: admin
 
登陆成功后即可开始体验KUN的所有功能。
