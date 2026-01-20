# 🚀 Microservices Implementation Guide
## Step-by-Step Guide to Transform Your Monolithic Banking App

---

## 📋 Prerequisites

Before starting, ensure you have:
- ✅ Java 21 installed
- ✅ Maven 3.8+ installed
- ✅ Docker & Docker Compose installed
- ✅ IDE (IntelliJ IDEA recommended)
- ✅ Git installed
- ✅ Basic knowledge of Spring Boot and Spring Cloud

---

## 🏗️ Step 1: Create Multi-Module Maven Project

### 1.1 Create Parent POM

Create a new `pom.xml` at the root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bellagnech</groupId>
    <artifactId>digital-banking-microservices</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Digital Banking Microservices</name>
    <description>Microservices architecture for Digital Banking</description>

    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.4.5</spring-boot.version>
        <spring-cloud.version>2024.0.0</spring-cloud.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>api-gateway</module>
        <module>service-discovery</module>
        <module>config-server</module>
        <module>auth-service</module>
        <module>customer-service</module>
        <module>account-service</module>
        <module>transaction-service</module>
        <module>reporting-service</module>
        <module>notification-service</module>
        <module>common</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

---

## 🔧 Step 2: Set Up Service Discovery (Eureka)

### 2.1 Create Eureka Server Module

**Directory**: `service-discovery/`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>digital-banking-microservices</artifactId>
        <groupId>com.bellagnech</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>service-discovery</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Application.java**:
```java
package com.bellagnech.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }
}
```

**application.yml**:
```yaml
server:
  port: 8761

spring:
  application:
    name: service-discovery

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

---

## 🔐 Step 3: Set Up Config Server

### 3.1 Create Config Server Module

**Directory**: `config-server/`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>digital-banking-microservices</artifactId>
        <groupId>com.bellagnech</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>config-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Application.java**:
```java
package com.bellagnech.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**application.yml**:
```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: file://${user.home}/digital-banking-config
          clone-on-start: true
      native:
        search-locations: classpath:/config

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## 🌐 Step 4: Set Up API Gateway

### 4.1 Create API Gateway Module

**Directory**: `api-gateway/`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>digital-banking-microservices</artifactId>
        <groupId>com.bellagnech</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>api-gateway</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Application.java**:
```java
package com.bellagnech.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

**application.yml**:
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1

        - id: customer-service
          uri: lb://customer-service
          predicates:
            - Path=/api/customers/**
          filters:
            - StripPrefix=1

        - id: account-service
          uri: lb://account-service
          predicates:
            - Path=/api/accounts/**
          filters:
            - StripPrefix=1

        - id: transaction-service
          uri: lb://transaction-service
          predicates:
            - Path=/api/transactions/**
          filters:
            - StripPrefix=1

        - id: reporting-service
          uri: lb://reporting-service
          predicates:
            - Path=/api/reports/**
          filters:
            - StripPrefix=1

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## 🔑 Step 5: Extract Auth Service

### 5.1 Create Auth Service Module

**Directory**: `auth-service/`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>digital-banking-microservices</artifactId>
        <groupId>com.bellagnech</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>auth-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

**application.yml**:
```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?createDatabaseIfNotExist=true
    username: root
    password: rootpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000
```

**Copy from existing project**:
- `AuthService.java`
- `JwtService.java`
- `AuthController.java`
- `User.java` entity
- `UserRepository.java`
- Security configuration

---

## 👥 Step 6: Extract Customer Service

### 6.1 Create Customer Service Module

**Directory**: `customer-service/`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <artifactId>digital-banking-microservices</artifactId>
        <groupId>com.bellagnech</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>customer-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

**application.yml**:
```yaml
server:
  port: 8082

spring:
  application:
    name: customer-service
  datasource:
    url: jdbc:mysql://localhost:3306/customer_db?createDatabaseIfNotExist=true
    username: root
    password: rootpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

**Feign Client for Auth Service**:
```java
package com.bellagnech.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
    
    @GetMapping("/api/auth/user/{username}")
    UserDTO getUser(@PathVariable String username);
}
```

---

## 💳 Step 7: Extract Account Service

### 7.1 Create Account Service Module

Similar structure to Customer Service, but:
- Port: 8083
- Database: `account_db`
- Dependencies on Customer Service (via Feign)

**application.yml**:
```yaml
server:
  port: 8083

spring:
  application:
    name: account-service
  datasource:
    url: jdbc:mysql://localhost:3306/account_db?createDatabaseIfNotExist=true
    username: root
    password: rootpassword
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 💰 Step 8: Extract Transaction Service

### 8.1 Create Transaction Service Module

**Key Features**:
- Port: 8084
- Database: `transaction_db`
- Event-driven with Kafka/RabbitMQ
- Saga pattern for distributed transactions

**Add Kafka Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-kafka</artifactId>
</dependency>
```

**Event Publisher**:
```java
package com.bellagnech.transaction.event;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {
    private final StreamBridge streamBridge;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        streamBridge.send("transactionCreated-out-0", event);
    }
}
```

---

## 📊 Step 9: Docker Compose Setup

### 9.1 Create docker-compose.yml

```yaml
version: '3.8'

services:
  # Databases
  mysql-auth:
    image: mysql:8.0
    container_name: mysql-auth
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: auth_db
    ports:
      - "3307:3306"
    volumes:
      - mysql_auth_data:/var/lib/mysql

  mysql-customer:
    image: mysql:8.0
    container_name: mysql-customer
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: customer_db
    ports:
      - "3308:3306"
    volumes:
      - mysql_customer_data:/var/lib/mysql

  mysql-account:
    image: mysql:8.0
    container_name: mysql-account
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: account_db
    ports:
      - "3309:3306"
    volumes:
      - mysql_account_data:/var/lib/mysql

  mysql-transaction:
    image: mysql:8.0
    container_name: mysql-transaction
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: transaction_db
    ports:
      - "3310:3306"
    volumes:
      - mysql_transaction_data:/var/lib/mysql

  # Service Discovery
  eureka:
    build: ./service-discovery
    container_name: eureka-server
    ports:
      - "8761:8761"
    networks:
      - banking-network

  # Config Server
  config-server:
    build: ./config-server
    container_name: config-server
    ports:
      - "8888:8888"
    depends_on:
      - eureka
    networks:
      - banking-network

  # API Gateway
  api-gateway:
    build: ./api-gateway
    container_name: api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka
      - config-server
    networks:
      - banking-network

  # Services
  auth-service:
    build: ./auth-service
    container_name: auth-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql-auth
      - eureka
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-auth:3306/auth_db
    networks:
      - banking-network

  customer-service:
    build: ./customer-service
    container_name: customer-service
    ports:
      - "8082:8082"
    depends_on:
      - mysql-customer
      - eureka
      - auth-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-customer:3306/customer_db
    networks:
      - banking-network

volumes:
  mysql_auth_data:
  mysql_customer_data:
  mysql_account_data:
  mysql_transaction_data:

networks:
  banking-network:
    driver: bridge
```

---

## 🧪 Step 10: Testing Strategy

### 10.1 Unit Tests

Each service should have:
- Service layer tests
- Repository tests
- Controller tests

### 10.2 Integration Tests

Use Testcontainers for database testing:
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

### 10.3 Contract Testing

Use Spring Cloud Contract for service-to-service testing.

---

## 📝 Step 11: Common Module

### 11.1 Create Common Module

**Directory**: `common/`

**Sub-modules**:
- `common-dto/` - Shared DTOs
- `common-exception/` - Exception handling
- `common-security/` - Security utilities

---

## 🚀 Step 12: Running the Services

### 12.1 Start Services in Order

1. **Start Infrastructure**:
```bash
# Start Eureka
cd service-discovery
mvn spring-boot:run

# Start Config Server
cd config-server
mvn spring-boot:run
```

2. **Start Services**:
```bash
# Start Auth Service
cd auth-service
mvn spring-boot:run

# Start Customer Service
cd customer-service
mvn spring-boot:run

# Start Account Service
cd account-service
mvn spring-boot:run

# Start Transaction Service
cd transaction-service
mvn spring-boot:run
```

3. **Start API Gateway**:
```bash
cd api-gateway
mvn spring-boot:run
```

### 12.2 Using Docker Compose

```bash
docker-compose up -d
```

---

## ✅ Checklist

### Phase 1: Infrastructure
- [ ] Parent POM created
- [ ] Eureka Server running
- [ ] Config Server running
- [ ] API Gateway running
- [ ] Docker Compose setup

### Phase 2: Services
- [ ] Auth Service extracted
- [ ] Customer Service extracted
- [ ] Account Service extracted
- [ ] Transaction Service extracted
- [ ] Reporting Service extracted
- [ ] Notification Service extracted

### Phase 3: Communication
- [ ] Service-to-service communication (Feign)
- [ ] Event-driven architecture (Kafka)
- [ ] Circuit breakers implemented
- [ ] Retry logic implemented

### Phase 4: Testing
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] Contract tests written
- [ ] Load tests performed

---

## 🎯 Next Steps

1. **Start with Phase 1** - Set up infrastructure
2. **Extract Auth Service first** - Least dependencies
3. **Gradually extract other services** - One at a time
4. **Test thoroughly** - After each extraction
5. **Deploy incrementally** - Don't rush

---

**Good luck with your microservices journey! 🚀**


