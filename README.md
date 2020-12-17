# KUN

### Build a single source of trust with a simple and powerful data governance platform.

Kun is a open source data governance platform.

## Quick Start
The easiest way to set up KUN is use docker-compose.
1. Enter project directory.
``` bash
cd KUN
```

2. If you want to use docker image from dockerhub, change `.env` file setting:
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
3. Use docker-compose command to set up KUN, including all the modules above.
```
docker-compose up
```
4. Visit http://localhost:9801  
 - username: admin
 - password: admin

## Usage
1. Config Data storage and computation engine
2. Setup User Access Management
3. Setup Data discovery
4. Setup Data Quality
5. Write a Hello World Pipeline
5. Good to go!

## Contributing
We open source Kun because it solve our own data management problem. We hope the other organizations can benefit from the project as well. We are thankful for any contributions from the community.

### [Code of Conduct](CODE_OF_CONDUCT.md)

We are very serious about the code of conduct. Please read the full text so that you can understand what actions will and will not be tolerated.

# Contact

We have a few channels for contact:
* Wechat Group
* Wechat Account
* Twitter
* Facebook Page


# Contributors
This project exists thanks to all the people who contribute.

# Sponsors

## Miotech
## ...
## ...

## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)