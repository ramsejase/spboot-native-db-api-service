# Read Me First

### GraalVM + Native + Spring AOT + Native + WebFlux + Docker + AutoConfiguration Library + Custom Bean Registration + Custom Qualifier Autowire

A typical example to externalize the database connection components from the service application and integrate the library as a spring-boot-starter library running as an executable in the docker container

#### Environment

- GraalVM 17.0.9
- SpringBoot 3.2.4
- Docker
- Windows WSL2 (Ubuntu Distro)
- Visual Studio Code

#### Running the example

Pre-requsite: Run the docker before the build
```
docker build -t db-api-service:1.0 -f Dockerfile .
```
without docker

```
# go to parent directory to compile the both library, application code
mvn clean install

# switch application folder
cd db-api-service

# now, run native build

./mvnw native:compile -Pnative
```
# run the native app
./target/db-api-service

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.4/maven-plugin/reference/html/#build-image)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/3.2.4/reference/htmlsingle/index.html#appendix.configuration-metadata.annotation-processor)

