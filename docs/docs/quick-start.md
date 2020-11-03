## Quick Start
The easiest way to set up KUN is use docker-compose.
1. Enter project directory.
``` bash
cd KUN
```
2. Use docker-compose command to set up KUN, including all the modules below.
```
docker-compose up
```

3. If you want to use docker image from dockerhub, change `.env` file setting:
```
IMAGE_REMOTE_ORG=miotechoss/ 
```
or you can build image from local Dockerfile in each module:
 - kun-data-discovery
```bash
./gradlew kun-data-discovery:bootJar
cd kun-data-discovery
docker build -t kun-data-discovery:master -f Dockerfile .
cd -
```
 - kun-data-dashboard
```bash
./gradlew kun-data-dashboard:bootJar
cd kun-data-dashboard
docker build -t kun-data-dashboard:master -f Dockerfile .
cd -
```
 - kun-data-platform
```bash
./gradlew kun-data-platform:kun-data-platform-web:bootJar
cd kun-data-platform
docker build -t kun-data-platform:master -f Dockerfile .
cd -
```
 - kun-frontend
```bash
./gradlew kun-frontend:yarnBuild
cd kun-data-discovery
docker build -t kun-frontend:master -f Dockerfile .
cd -
```
 - kun-metadata
```bash
./gradlew kun-metadata:shadowJar
cd kun-data-metadata
docker build -t kun-metadata:master -f Dockerfile .
cd -
```
 - kun-workflow
```bash
./gradlew kun-workflow:shadowJar
cd kun-workflow
docker build -t kun-workflow:master -f Dockerfile .
cd -
```
 - kun-security
```bash
./gradlew kun-security:kun-security-server:bootJar
cd kun-security
docker build -t kun-security:master -f Dockerfile .
cd -
```

4. Visit http://localhost:9801  
 - username: admin
 - password: admin
