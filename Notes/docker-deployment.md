# Docker Deployment Steps (Spring Boot + MySQL)

This guide demonstrates how to run a Spring Boot application and a MySQL database using Docker containers connected through the same Docker network.

---

# Architecture

```text
                   Docker Network (springboot-network)

        +------------------------------------------------------+

        +-------------------------+      +----------------------+
        | Spring Boot Container   | ---> | MySQL Container      |
        | springboot-api-v1       |      | mysql-db-v1          |
        +-------------------------+      +----------------------+

        +------------------------------------------------------+
```

The Spring Boot application communicates with MySQL using the **MySQL container name** as the hostname.

```
Host = mysql-db-v1
```

---

# Step 1: Build Spring Boot Application

```bash
gradle clean build
```

Verify the generated JAR.

```bash
ls build/libs
```

---

# Step 2: Build Spring Boot Docker Image

```bash
docker build -t piyushpingale23/springboot-api:1.0 .
```

Verify the image.

```bash
docker images
```

Expected:

```text
REPOSITORY                          TAG
piyushpingale23/springboot-api  1.0
```

---

# Step 3: Pull MySQL Image

```bash
docker pull piyushpingale23/mysql:8
```

Verify:

```bash
docker images
```

---

# Step 4: Create Docker Network

```bash
docker network create springboot-network
```

Verify.

```bash
docker network ls
```

Expected:

```text
NETWORK ID     NAME
xxx            springboot-network
```

---

# Step 5: Run MySQL Container

```bash
docker run -d \
--name mysql-db-v1 \
--network springboot-network \
-e MYSQL_ROOT_PASSWORD=Root@123 \
-e MYSQL_DATABASE=springboot_db \
-p 3306:3306 \
piyushpingale23/mysql:8
```

Verify.

```bash
docker ps
```

---

# Step 6: Run Spring Boot Container

```bash
docker run -d \
--name springboot-api-v1 \
--network springboot-network \
-p 8080:8080 \
piyushpingale23/springboot-api:1.0
```

Verify.

```bash
docker ps
```

---

# Step 7: Verify Running Containers

```bash
docker ps
```

Expected:

```text
CONTAINER ID   IMAGE
xxx            piyushpingale23/springboot-api:1.0
xxx            piyushpingale23/mysql:8
```

---

# Step 8: Verify Docker Network

```bash
docker network inspect springboot-network
```

Expected:

```text
Containers

springboot-api-v1

mysql-db-v1
```

---

# Step 9: Access the Spring Boot Application

Open your browser:

```text
http://localhost:8080/api/learners
```

---

# Step 10: Connect to MySQL

```bash
docker exec -it mysql-db-v1 mysql -u root -p
```

Password:

```text
Root@123
```

Run:

```sql
SHOW DATABASES;

USE springboot_db;

SHOW TABLES;
```

---

# Spring Boot application.properties

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://mysql-db-v1:3306/springboot_db?createDatabaseIfNotExist=true

spring.datasource.username=root

spring.datasource.password=Root@123

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.format_sql=true
```

---

# Why does `mysql-db-v1` work as the hostname?

Both containers are attached to the same Docker network (`springboot-network`).

Docker provides an internal DNS service that automatically resolves the container name to its IP address.

```text
Spring Boot Container
        │
        ▼
Host = mysql-db-v1
        │
        ▼
Docker Internal DNS
        │
        ▼
MySQL Container
```

Because of this, you do **not** need to know or configure the MySQL container's IP address.

---

# Useful Docker Commands

## View running containers

```bash
docker ps
```

---

## View all containers

```bash
docker ps -a
```

---

## View Docker images

```bash
docker images
```

---

## View container logs

Spring Boot:

```bash
docker logs springboot-api-v1
```

MySQL:

```bash
docker logs mysql-db-v1
```

---

## Stop containers

```bash
docker stop springboot-api-v1

docker stop mysql-db-v1
```

---

## Start containers

```bash
docker start springboot-api-v1

docker start mysql-db-v1
```

---

## Restart containers

```bash
docker restart springboot-api-v1

docker restart mysql-db-v1
```

---

## Remove containers

```bash
docker rm -f springboot-api-v1

docker rm -f mysql-db-v1
```

---

## Remove Docker network

```bash
docker network rm springboot-network
```

---

# Complete Deployment Commands

```bash
# Build Spring Boot
gradle clean build

# Build Spring Boot Image
docker build -t piyushpingale23/springboot-api:1.0 .

# Pull MySQL Image
docker pull piyushpingale23/mysql:8

# Create Docker Network
docker network create springboot-network

# Run MySQL Container
docker run -d \
--name mysql-db-v1 \
--network springboot-network \
-e MYSQL_ROOT_PASSWORD=Root@123 \
-e MYSQL_DATABASE=springboot_db \
-p 3306:3306 \
piyushpingale23/mysql:8

# Run Spring Boot Container
docker run -d \
--name springboot-api-v1 \
--network springboot-network \
-p 8080:8080 \
piyushpingale23/springboot-api:1.0

# Verify Containers
docker ps

# Verify Network
docker network inspect springboot-network

# Access Application
http://localhost:8080/api/learners

# Connect to MySQL
docker exec -it mysql-db-v1 mysql -u root -p
```

---

# Deployment Flow

```text
Spring Boot Source Code
            │
            ▼
      Build JAR
            │
            ▼
 Build Docker Image
            │
            ▼
Create Docker Network
(springboot-network)
            │
            ├────────────────────► Run MySQL Container
            │                     (mysql-db-v1)
            │
            └────────────────────► Run Spring Boot Container
                                  (springboot-api-v1)
                                            │
                                            ▼
                           Connect using Host = mysql-db-v1
                                            │
                                            ▼
                               Docker Internal DNS
                                            │
                                            ▼
                                  MySQL Database
                                            │
                                            ▼
                           Access Application

                   http://localhost:8080/api/learners
```