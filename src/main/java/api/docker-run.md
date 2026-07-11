# Spring Boot + MySQL Docker Setup (Step-by-Step)

This guide explains how to:

- Create a Docker network
- Create a MySQL container
- Build a Spring Boot Docker image
- Run the Spring Boot container
- Connect Spring Boot with MySQL
- Verify everything is working

---

# Prerequisites

Install the following:

- Docker Desktop / Docker Engine
- Java 21
- Gradle 8.5
- Spring Boot 3.2
- Dockerfile
- settings.gradle

Verify Docker

```bash
docker --version
docker ps
```

---

# Project Structure

```
spring-framework/
│
├── Dockerfile
├── .dockerignore
├── build.gradle
├── settings.gradle
├── gradlew
├── gradle/
├── src/
└── ...
```

---

# settings.gradle

```groovy
rootProject.name = 'spring-framework'
```

---

# build.gradle

Specify your Spring Boot Main class.

```groovy
springBoot {
    mainClass = 'api.ApiMain'
}
```

---

# application.properties

Since MySQL runs inside Docker, **do not use localhost**.

```properties
server.port=8090

spring.datasource.url=jdbc:mysql://lms-mysql-db:3306/lms?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=Root@123

spring.jpa.hibernate.ddl-auto=update
```

Notice:

```
lms-mysql-db
```

is the MySQL container name.

---

# Dockerfile

```dockerfile
# ==================================
# Stage 1 - Build Application
# ==================================

FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle clean bootJar --no-daemon

# ==================================
# Stage 2 - Runtime
# ==================================

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]
```

---

# .dockerignore

```
.gradle
build
.idea
.vscode
.git
.gitignore
*.iml
README.md
```

---

# STEP 1 : Verify Docker

```bash
docker --version
docker ps
```

---

# STEP 2 : Go to Project Directory

```bash
cd ~/Project/AT/spring-framework
```

Verify

```bash
pwd

ls
```

Expected

```
Dockerfile
build.gradle
settings.gradle
gradlew
gradle
src
```

---

# STEP 3 : Create Docker Network

```bash
docker network create lms-network
```

Verify

```bash
docker network ls
```

Expected

```
lms-network
```

---

# STEP 4 : Pull MySQL Image

```bash
docker pull mysql:8
```

Verify

```bash
docker images
```

---

# STEP 5 : Create MySQL Container

```bash
docker run -d \
--name lms-mysql-db \
--network lms-network \
-v lms-mysql-data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=Root@123 \
-e MYSQL_DATABASE=lms \
-p 3306:3306 \
mysql:8
```

Verify

```bash
docker ps
```

Expected

```
lms-mysql-db
```

---

# STEP 6 : Verify MySQL Logs

```bash
docker logs -f lms-mysql-db
```

Wait until you see

```
ready for connections
```

Press

```
CTRL + C
```

---

# STEP 7 : Build Spring Boot Docker Image

Run from project root.

```bash
docker build -t lms-springboot-api:1.0 .
```

---

# STEP 8 : Verify Spring Boot Image

```bash
docker images
```

Expected

```
REPOSITORY

lms-springboot-api
mysql
```

---

# STEP 9 : Run Spring Boot Container

```bash
docker run -d \
--name lms-springboot-api \
--network lms-network \
-p 8090:8090 \
lms-springboot-api:1.0
```

---

# STEP 10 : Verify Running Containers

```bash
docker ps
```

Expected

```
lms-mysql-db

lms-springboot-api
```

---

# STEP 11 : Verify Network

```bash
docker network inspect lms-network
```

Expected

Both containers should appear.

```
lms-mysql-db

lms-springboot-api
```

---

# STEP 12 : Verify Spring Boot Logs

```bash
docker logs -f lms-springboot-api
```

Look for

```
Started ApiMain
```

Press

```
CTRL + C
```

---

# STEP 13 : Test Application

Open

```
http://localhost:8090
```

or

```
http://localhost:8090/actuator/health
```

---

# STEP 14 : Stop Containers

```bash
docker stop lms-springboot-api
docker stop lms-mysql-db
```

---

# STEP 15 : Start Containers Again

```bash
docker start lms-mysql-db
docker start lms-springboot-api
```

Verify

```bash
docker ps
```

---

# STEP 16 : Remove Containers

```bash
docker rm -f lms-springboot-api
docker rm -f lms-mysql-db
```

Verify

```bash
docker ps -a
```

---

# STEP 17 : Remove Images

```bash
docker rmi lms-springboot-api:1.0
docker rmi mysql:8
```

Verify

```bash
docker images
```

---

# STEP 18 : Remove Network

```bash
docker network rm lms-network
```

Verify

```bash
docker network ls
```

---

# Useful Commands

## Running Containers

```bash
docker ps
```

---

## All Containers

```bash
docker ps -a
```

---

## Images

```bash
docker images
```

---

## Networks

```bash
docker network ls
```

---

## Inspect Network

```bash
docker network inspect lms-network
```

---

## Container Logs

```bash
docker logs -f lms-springboot-api

docker logs -f lms-mysql-db
```

---

## Execute Command Inside Container

Spring Boot

```bash
docker exec -it lms-springboot-api /bin/bash
```

MySQL

```bash
docker exec -it lms-mysql-db mysql -uroot -p
```

Password

```
Root@123
```

---

# Architecture

```
                     Docker Host

                           │

                  Docker Network
                     lms-network

         ┌────────────────────────────────┐
         │                                │
         ▼                                ▼

+-----------------------+      +----------------------+
| lms-springboot-api    | ---> | lms-mysql-db         |
|                       |      |                      |
| Spring Boot           |      | MySQL 8             |
| Port : 8090           |      | Port : 3306         |
+-----------------------+      +----------------------+
```

---

# Important Notes

1. Always build the image from the project root.

```
Dockerfile
build.gradle
settings.gradle
src
```

must exist in the current directory.

---

2. Spring Boot should **never** connect to

```
localhost
```

inside Docker.

Instead use

```
lms-mysql-db
```

because Docker containers communicate using the container name on the same Docker network.

---

3. Both containers **must** be connected to

```
lms-network
```

---

4. Build Image

```bash
docker build -t lms-springboot-api:1.0 .
```

---

5. Run MySQL

```bash
docker run ...
```

---

6. Run Spring Boot

```bash
docker run ...
```

---

7. Verify

```bash
docker ps

docker logs

docker network inspect lms-network
```

If all of the above work, your Spring Boot application is successfully communicating with the MySQL container through the Docker network.