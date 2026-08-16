# OENEXA Core™ — Development Process & Step-by-Step Design Guide

> **Document Type:** Development Process Guide  
> **Version:** 2.0.0  
> **Date:** September 19, 2026  
> **Purpose:** Complete step-by-step development design process for the OENEXA Core (oenexa-core) multi-module platform  

---

## Table of Contents

1. [Project Architecture Overview](#1-project-architecture-overview)
2. [Multi-Module Project Structure](#2-multi-module-project-structure)
3. [Build System Design](#3-build-system-design)
4. [Shared Libraries Design](#4-shared-libraries-design)
5. [Microservice Module Design Pattern](#5-microservice-module-design-pattern)
6. [Database Design Process](#6-database-design-process)
7. [Event-Driven Architecture Design](#7-event-driven-architecture-design)
8. [Security Architecture Design](#8-security-architecture-design)
9. [API Design Standards](#9-api-design-standards)
10. [Infrastructure Design](#10-infrastructure-design)
11. [CI/CD Pipeline Design](#11-cicd-pipeline-design)
12. [Phase-by-Phase Development Plan](#12-phase-by-phase-development-plan)
13. [Service Dependency Graph](#13-service-dependency-graph)
14. [Port Allocation Map](#14-port-allocation-map)
15. [Database Allocation Map](#15-database-allocation-map)
16. [Kafka Topic Registry](#16-kafka-topic-registry)
17. [Development Environment Setup](#17-development-environment-setup)
18. [Coding Standards & Conventions](#18-coding-standards--conventions)
19. [Testing Strategy](#19-testing-strategy)
20. [Deployment Strategy](#20-deployment-strategy)

---

## 1. Project Architecture Overview

### Design Philosophy

OENEXA follows a **Domain-Driven Design (DDD)** approach with **microservices architecture**. Each bounded context is implemented as an independent deployable service with its own database (**Database per Service** pattern).

### Architecture Principles

| Principle                 | Implementation                                             |
|---------------------------|------------------------------------------------------------|
| **Single Responsibility** | Each service owns one business domain                      |
| **Database per Service**  | No shared databases between services                       |
| **Event-Driven**          | Kafka for async inter-service communication                |
| **API Gateway**           | Single entry point with JWT validation                     |
| **Shared Nothing**        | Services communicate only via APIs/events                  |
| **Fail Fast**             | Circuit breakers (Resilience4j) on all inter-service calls |
| **Observability**         | Prometheus + Grafana + ELK + Jaeger on every service       |

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ Web App  │  │Mobile App│  │ API Client│  │Admin Panel│              │
│  │(React 19)│  │(RN)      │  │(REST/WS) │  │(React)   │              │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘              │
└───────┼──────────────┼──────────────┼──────────────┼────────────────────┘
        │              │              │              │
        └──────────────┼──────────────┼──────────────┘
                       │              │
                       ▼              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY LAYER (:8080)                            │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐             │
│  │Rate Limit │ │JWT Verify │ │  Routing  │ │Circuit Brk│             │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘             │
└─────────────────────────────┬───────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     MICROSERVICES LAYER                                  │
│                                                                         │
│  ┌─────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────────┐      │
│  │  Identity   │ │    User    │ │    KYC    │ │    Wallet     │      │
│  │  :8081      │ │   :8082    │ │   :8083   │ │    :8084      │      │
│  └─────────────┘ └────────────┘ └───────────┘ └───────────────┘      │
│                                                                         │
│  ┌─────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────────┐      │
│  │  Trading    │ │  Matching  │ │  Payment  │ │   Security    │      │
│  │  :8085      │ │  :8086     │ │   :8087   │ │    :8088      │      │
│  └─────────────┘ └────────────┘ └───────────┘ └───────────────┘      │
│                                                                         │
│  ┌─────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────────┐      │
│  │Notification │ │ Reporting  │ │   Audit   │ │ Market Data   │      │
│  │  :8089      │ │  :8090     │ │   :8091   │ │    :8092      │      │
│  └─────────────┘ └────────────┘ └───────────┘ └───────────────┘      │
│                                                                         │
│  ┌─────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────────┐      │
│  │   Admin     │ │Risk Engine │ │ Analytics │ │   Banking     │      │
│  │   :8093     │ │   :8094    │ │   :8095   │ │    :8096      │      │
│  └─────────────┘ └────────────┘ └───────────┘ └───────────────┘      │
└─────────────────────────────┬───────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                       │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐             │
│  │ MySQL 9+  │ │ Redis 8+  │ │  Kafka 4+ │ │Elastic 9+ │             │
│  │(10 DBs)   │ │(Cache/Sess│ │(Events)   │ │(Search)   │             │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘             │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Multi-Module Project Structure

### Step 1: Define the Module Hierarchy

```
oenexa-core/                             ← Root Project (Gradle parent)
│
├── gradle/
│   └── libs.versions.toml               ← Centralized Version Catalog
│
├── build.gradle.kts                     ← Parent build (shared conventions)
├── settings.gradle.kts                  ← Module registration (19 modules)
│
├── oenexa-common/                       ← Shared DTOs, entities, utils, enums
├── oenexa-security-common/              ← Shared JWT, security filters, auth
│
├── oenexa-api-gateway/                  ← Spring Cloud Gateway (:8080)
├── oenexa-identity-service/             ← Auth, OAuth2, MFA (:8081)
├── oenexa-user-service/                 ← User profiles (:8082)
├── oenexa-kyc-service/                  ← KYC/AML (:8083)
├── oenexa-wallet-service/               ← Wallets & transfers (:8084)
├── oenexa-trading-service/              ← Order management (:8085)
├── oenexa-matching-engine/              ← Order matching (:8086)
├── oenexa-payment-service/              ← Payments (:8087)
├── oenexa-security-service/             ← Fraud detection (:8088)
├── oenexa-notification-service/         ← Notifications (:8089)
├── oenexa-reporting-service/            ← Reports (:8090)
├── oenexa-audit-service/                ← Audit logs (:8091)
├── oenexa-market-data-service/          ← Market data (:8092)
├── oenexa-admin-service/                ← Admin portal (:8093)
├── oenexa-risk-engine/                  ← Risk scoring (:8094)
├── oenexa-analytics-service/            ← Analytics (:8095)
├── oenexa-banking-service/              ← Banking (:8096)
│
├── docker-compose.yml                   ← Local infrastructure
├── scripts/
│   └── init-databases.sql               ← DB initialization
├── kubernetes/                          ← K8s manifests
├── terraform/                           ← Infrastructure as Code
└── .github/workflows/                   ← CI/CD pipelines
```

### Step 2: Module Dependency Graph

```
                    oenexa-common
                         │
                         ▼
                oenexa-security-common
                         │
            ┌────────────┼────────────┐
            ▼            ▼            ▼
    Gateway Service   Service N   Service M
      (:8080)        (any svc)   (any svc)

Exception: oenexa-matching-engine depends only on oenexa-common (no security)
```

---

## 3. Build System Design

### Step 3: Version Catalog (gradle/libs.versions.toml)

Centralizes ALL dependency versions in a single file:

| Category      | Key Dependencies                                  |
|---------------|---------------------------------------------------|
| **Core**      | Java 26, Spring Boot 4.1.0, Spring Cloud 2025.0.0 |
| **Data**      | MySQL Connector 9.3, Flyway 11.9, Hibernate 7.1   |
| **Messaging** | Spring Kafka 4.0, Redis/Lettuce                   |
| **Security**  | JJWT 0.12.6, BouncyCastle 1.80, Argon2            |
| **Mapping**   | MapStruct 1.6.3, Lombok 1.18.38                   |
| **Testing**   | Testcontainers 1.21, MockMvc, REST Assured        |

### Step 4: Root build.gradle.kts

The root build:
- Applies `spring-boot` and `spring-dependency-management` with `apply false`
- Configures ALL subprojects with:
  - Java 26 toolchain
  - Lombok + MapStruct annotation processors
  - Common test dependencies (JUnit 5)
  - UTF-8 encoding, `-parameters` compiler flag

### Step 5: Module build.gradle.kts Pattern

**Shared libraries** use `java-library` plugin only:
```kotlin
plugins {
    `java-library`
}
dependencies {
    api(rootProject.libs.spring.boot.starter.validation)
    // ...
}
```

**Service modules** apply Spring Boot:
```kotlin
plugins {
    alias(rootProject.libs.plugins.spring.boot)
}
dependencies {
    implementation(project(":oenexa-common"))
    implementation(project(":oenexa-security-common"))
    implementation(rootProject.libs.spring.boot.starter.web)
    // ...
}
```

---

## 4. Shared Libraries Design

### Step 6: oenexa-common Library

| Package     | Purpose                  | Key Classes                                                                                                    |
|-------------|--------------------------|----------------------------------------------------------------------------------------------------------------|
| `entity`    | Base JPA entities        | `BaseEntity` (id, uuid, timestamps), `AuditableEntity`                                                         |
| `dto`       | Common response wrappers | `ApiResponse<T>`, `PageResponse<T>`, `ErrorResponse`                                                           |
| `exception` | Custom exceptions        | `BusinessException`, `ResourceNotFoundException`, `DuplicateResourceException`, `InsufficientBalanceException` |
| `util`      | Utility classes          | `DateUtils`, `CryptoUtils`, `ValidationUtils`                                                                  |
| `constant`  | Enums & constants        | `AccountStatus`, `KycStatus`, `WalletType`, `OrderType`, `OrderSide`, `TransactionType`, `RiskLevel`, etc.     |
| `event`     | Kafka event models       | `BaseEvent`, `DomainEvent`, `EventTopics`                                                                      |

### Step 7: oenexa-security-common Library

| Package      | Purpose            | Key Classes                                                    |
|--------------|--------------------|----------------------------------------------------------------|
| `jwt`        | JWT management     | `JwtTokenProvider`, `JwtAuthenticationFilter`, `JwtProperties` |
| `model`      | Auth models        | `UserPrincipal`, `AuthenticatedUser`                           |
| `annotation` | Custom annotations | `@CurrentUser`                                                 |
| `config`     | Security constants | `SecurityConstants`                                            |

---

## 5. Microservice Module Design Pattern

### Step 8: Standard Service Package Layout

Every service follows this consistent structure:

```
oenexa-{service-name}/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/org/oenexa/{domain}/
    │   │   ├── {Domain}ServiceApplication.java    ← @SpringBootApplication
    │   │   ├── config/                             ← @Configuration beans
    │   │   ├── controller/                         ← @RestController (API layer)
    │   │   ├── service/                            ← Business logic interfaces
    │   │   │   └── impl/                           ← @Service implementations
    │   │   ├── repository/                         ← Spring Data JPA interfaces
    │   │   ├── entity/                             ← @Entity JPA classes
    │   │   ├── dto/
    │   │   │   ├── request/                        ← Inbound request DTOs
    │   │   │   └── response/                       ← Outbound response DTOs
    │   │   ├── mapper/                             ← MapStruct @Mapper interfaces
    │   │   ├── exception/                          ← Service-specific exceptions
    │   │   └── kafka/
    │   │       ├── producer/                       ← Event publishers
    │   │       └── consumer/                       ← Event listeners
    │   └── resources/
    │       ├── application.yml                     ← Service configuration
    │       └── db/migration/                       ← Flyway SQL migrations
    └── test/
        └── java/org/oenexa/{domain}/
            ├── controller/                         ← Controller tests (MockMvc)
            ├── service/                            ← Service unit tests
            └── repository/                         ← Repository tests (@DataJpaTest)
```

### Step 9: Layered Architecture per Service

```
┌───────────────────────────────────┐
│         Controller Layer          │  ← HTTP request handling, validation
│    (@RestController, @Valid)      │
├───────────────────────────────────┤
│          Service Layer            │  ← Business logic, orchestration
│    (@Service, @Transactional)     │
├───────────────────────────────────┤
│        Repository Layer           │  ← Data access, queries
│    (JpaRepository, @Query)        │
├───────────────────────────────────┤
│          Entity Layer             │  ← JPA entities, domain model
│    (@Entity, @Table)              │
├───────────────────────────────────┤
│          Event Layer              │  ← Kafka producers & consumers
│    (@KafkaListener, KafkaTemplate)│
└───────────────────────────────────┘
```

---

## 6. Database Design Process

### Step 10: Database-per-Service Mapping

| Service              | Database          | Key Tables                                                                 | Engine |
|----------------------|-------------------|----------------------------------------------------------------------------|--------|
| Identity Service     | `identity_db`     | users, roles, permissions, user_roles, user_sessions, login_audit, devices | InnoDB |
| User Service         | `user_db`         | user_profiles                                                              | InnoDB |
| KYC Service          | `kyc_db`          | kyc_profiles, documents, verification_history                              | InnoDB |
| Wallet Service       | `wallet_db`       | wallets, wallet_balances, wallet_transactions, wallet_addresses            | InnoDB |
| Trading Service      | `trading_db`      | trading_pairs, markets, orders, trades, positions                          | InnoDB |
| Payment Service      | `payment_db`      | payments, payment_methods                                                  | InnoDB |
| Security Service     | `security_db`     | security_events, fraud_events, risk_scores, investigations                 | InnoDB |
| Notification Service | `notification_db` | notifications, email_logs, sms_logs                                        | InnoDB |
| Audit Service        | `audit_db`        | audit_logs, activity_logs                                                  | InnoDB |
| Banking Service      | `banking_db`      | bank_accounts, beneficiaries, transfers, ledger_entries                    | InnoDB |

### Step 11: Migration Strategy (Flyway)

- Each service manages its own migrations independently
- Migration files: `V{version}__{description}.sql`
- `spring.jpa.hibernate.ddl-auto=validate` (never auto-create in production)
- Flyway runs automatically on service startup

### Step 12: Key Database Design Decisions

| Decision                             | Rationale                                       |
|--------------------------------------|-------------------------------------------------|
| DECIMAL(36,18) for crypto amounts    | Supports 18 decimal places (ETH wei precision)  |
| DECIMAL(20,8) for fiat amounts       | Standard financial precision                    |
| VARCHAR(36) for UUIDs                | Cross-service reference IDs                     |
| @Version for wallet_balances         | Optimistic locking prevents race conditions     |
| Generated column for `total` balance | `total = available + locked`, always consistent |
| ENUM types                           | MySQL-native enums for type safety              |
| JSON columns for metadata            | Flexible extension without schema changes       |
| Encrypted fields (*_enc)             | Bank account numbers stored encrypted at rest   |

---

## 7. Event-Driven Architecture Design

### Step 13: Kafka Topic Registry

| Topic Name           | Producer         | Consumers                                 | Payload                               |
|----------------------|------------------|-------------------------------------------|---------------------------------------|
| `user.registered`    | Identity Service | Notification, Audit                       | userId, email, phone                  |
| `user.login`         | Identity Service | Security, Audit, Analytics                | userId, ip, device, riskScore         |
| `kyc.submitted`      | KYC Service      | Notification, Audit                       | userId, kycLevel                      |
| `kyc.approved`       | KYC Service      | Wallet (auto-create), Notification, Audit | userId, kycLevel, tier                |
| `wallet.deposit`     | Wallet Service   | Notification, Audit, Analytics            | userId, walletId, amount, currency    |
| `wallet.withdrawal`  | Wallet Service   | Notification, Audit, Risk, Analytics      | userId, walletId, amount              |
| `transfer.initiated` | Wallet Service   | Security (AML), Risk, Audit               | senderId, receiverId, amount          |
| `transfer.completed` | Wallet Service   | Notification, Audit, Analytics            | transferId, status                    |
| `order.created`      | Trading Service  | Matching Engine, Audit                    | orderId, pair, type, side, price, qty |
| `order.filled`       | Trading Service  | Notification, Audit, Analytics            | orderId, fillPrice, fillQty           |
| `trade.executed`     | Matching Engine  | Wallet, Market Data, Notification, Audit  | tradeId, buyerOrderId, sellerOrderId  |
| `payment.processed`  | Payment Service  | Wallet, Notification, Audit               | paymentId, amount, status             |
| `security.alert`     | Security Service | Notification, Audit, Risk                 | userId, alertType, severity           |
| `fraud.detected`     | Security Service | Notification, Audit, Admin                | userId, eventType, riskScore          |

### Step 14: Event Design Pattern

```java
// Base event structure
public abstract class BaseEvent {
    private String eventId;      // UUID
    private String eventType;    // e.g., "USER_REGISTERED"
    private LocalDateTime timestamp;
    private String source;       // e.g., "identity-service"
}

// Domain event with entity reference
public abstract class DomainEvent extends BaseEvent {
    private Long entityId;
    private String entityType;   // e.g., "USER", "ORDER", "WALLET"
}
```

---

## 8. Security Architecture Design

### Step 15: Authentication Flow

```
Client → API Gateway → Identity Service → JWT Issued
                 │
                 ├─ Rate Limiting (Redis)
                 ├─ JWT Validation (on subsequent requests)
                 ├─ CORS enforcement
                 └─ Route to target service
```

### Step 16: Security Layer Stack

| Layer             | Technology           | Implementation                     |
|-------------------|----------------------|------------------------------------|
| 1. Transport      | TLS 1.3              | All traffic encrypted              |
| 2. Gateway        | Spring Cloud Gateway | Rate limiting, WAF                 |
| 3. Authentication | JWT (RS256)          | 15-min access tokens               |
| 4. Authorization  | RBAC + ABAC          | 8 roles, method-level security     |
| 5. Data           | AES-256-GCM          | Sensitive fields encrypted at rest |
| 6. Password       | Argon2id             | Password hashing                   |
| 7. MFA            | TOTP (RFC 6238)      | Google Authenticator compatible    |
| 8. Monitoring     | ML models            | Real-time fraud detection          |

### Step 17: JWT Token Structure

```json
{
  "sub": "user-uuid",
  "iss": "oenexa",
  "iat": 1723456789,
  "exp": 1723457689,
  "roles": ["ROLE_USER", "ROLE_TRADER"],
  "userId": 12345,
  "email": "user@example.com",
  "kycTier": "TIER2",
  "accountTier": "PREMIUM"
}
```

---

## 9. API Design Standards

### Step 18: RESTful API Conventions

| Convention      | Standard                                             |
|-----------------|------------------------------------------------------|
| Base path       | `/api/v1/{resource}`                                 |
| Versioning      | URL-based (`/v1/`, `/v2/`)                           |
| Naming          | Lowercase, hyphens for multi-word (`/bank-accounts`) |
| Response format | Always `ApiResponse<T>` wrapper                      |
| Errors          | RFC 7807 Problem Details                             |
| Pagination      | Cursor-based for large datasets                      |
| Filtering       | Query params: `?status=ACTIVE&from=2026-01-01`       |

### Step 19: Standard Response Wrapper

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-08-12T10:00:00Z"
}
```

### Step 20: API Endpoint Registry

| Service     | Prefix                  | Key Endpoints                                                            |
|-------------|-------------------------|--------------------------------------------------------------------------|
| Identity    | `/api/v1/auth`          | register, login, verify-email, verify-phone, enable-mfa, refresh, logout |
| User        | `/api/v1/users`         | /me, /me/settings, /me/preferences                                       |
| KYC         | `/api/v1/kyc`           | /submit, /status, /upload-document                                       |
| Wallet      | `/api/v1/wallets`       | CRUD, /{id}/balance, /{id}/deposit, /{id}/withdraw                       |
| Transfers   | `/api/v1/transfers`     | /p2p, /wallet                                                            |
| Trading     | `/api/v1/orders`        | place, cancel, list, details                                             |
| Market Data | `/api/v1/market`        | /ticker, /orderbook, /klines, /24hr                                      |
| Payments    | `/api/v1/payments`      | /card, /bank-transfer, /sepa, /swift                                     |
| Banking     | `/api/v1/bank-accounts` | CRUD, /beneficiaries                                                     |
| Admin       | `/api/v1/admin`         | /users, /kyc, /transactions, /system                                     |
| Security    | `/api/v1/security`      | /events, /alerts                                                         |
| Reports     | `/api/v1/reports`       | /financial, /tax, /compliance                                            |
| Audit       | `/api/v1/audit`         | /logs, /activities                                                       |
| Analytics   | `/api/v1/analytics`     | /platform, /trading-volume, /revenue                                     |
| Risk        | `/api/v1/risk`          | /assess, /scores                                                         |

---

## 10. Infrastructure Design

### Step 21: Docker Compose (Local Development)

| Service         | Image                             | Port      | Purpose                          |
|-----------------|-----------------------------------|-----------|----------------------------------|
| MySQL 9.3       | `mysql:9.3`                       | 3306      | Primary database (10 schemas)    |
| Redis 8         | `redis:8-alpine`                  | 6379      | Caching, sessions, rate limiting |
| Zookeeper       | `confluentinc/cp-zookeeper:7.9.0` | 2181      | Kafka coordination               |
| Kafka           | `confluentinc/cp-kafka:7.9.0`     | 9092      | Event streaming                  |
| Elasticsearch 8 | `elastic/elasticsearch:8.18.3`    | 9200      | Search, analytics, logging       |
| MinIO           | `minio/minio:latest`              | 9000/9001 | S3-compatible object storage     |
| Kafka UI        | `provectuslabs/kafka-ui:latest`   | 8180      | Topic management UI              |

### Step 22: Kubernetes Architecture

```
kubernetes/
├── base/                           ← Common manifests
│   ├── namespace.yaml              ← dev, staging, prod namespaces
│   ├── configmap.yaml              ← Shared configuration
│   └── service-deployment-template.yaml  ← Parameterized template
└── overlays/
    ├── dev/                        ← Dev-specific overrides
    ├── staging/                    ← Staging overrides
    └── production/                 ← Production overrides (HA replicas)
```

---

## 11. CI/CD Pipeline Design

### Step 23: Pipeline Stages

```
Code Push → PR → CI Pipeline → Merge → CD Pipeline → Deploy
             │                            │
             ├── Build all modules         ├── Build Docker images
             ├── Run unit tests            ├── Push to ECR
             ├── Run integration tests     ├── Deploy to K8s (ArgoCD)
             ├── SonarQube analysis        └── Health check verification
             ├── Security scan (Trivy)
             └── Code review
```

### Step 24: Environment Promotion

| Environment | Branch      | Trigger                | URL                |
|-------------|-------------|------------------------|--------------------|
| Development | `develop`   | Push to develop        | dev.oenexa.com     |
| Staging     | `release/*` | Push to release branch | staging.oenexa.com |
| Production  | `main`      | Tag push (v*)          | app.oenexa.com     |

---

## 12. Phase-by-Phase Development Plan

### PHASE 1: FOUNDATION (Months 1–4)

| Step | Task                                  | Module(s)                             | Duration   |
|------|---------------------------------------|---------------------------------------|------------|
| 1    | Create multi-module Gradle project    | Root                                  | Week 1     |
| 2    | Configure shared libraries            | oenexa-common, oenexa-security-common | Week 1–2   |
| 3    | Set up Docker Compose infrastructure  | Infrastructure                        | Week 2     |
| 4    | Set up CI/CD pipelines                | .github/workflows                     | Week 2     |
| 5    | Implement API Gateway                 | oenexa-api-gateway                    | Week 3–4   |
| 6    | Build Identity Service (registration) | oenexa-identity-service               | Week 5–8   |
| 7    | Build Identity Service (login, MFA)   | oenexa-identity-service               | Week 9–10  |
| 8    | Build User Service                    | oenexa-user-service                   | Week 11–12 |
| 9    | Build KYC Service                     | oenexa-kyc-service                    | Week 13–16 |
| 10   | Build Notification Service (basic)    | oenexa-notification-service           | Week 13–14 |
| 11   | Build Audit Service (basic)           | oenexa-audit-service                  | Week 15–16 |

**Phase 1 Exit Criteria:**
- ✅ User registration with email + phone verification
- ✅ Login with MFA (TOTP)
- ✅ JWT token issuance (RS256)
- ✅ KYC document submission
- ✅ All events flowing through Kafka
- ✅ API Gateway routing all services

### PHASE 2: FINANCIAL CORE (Months 5–8)

| Step | Task                                  | Module(s)              | Duration   |
|------|---------------------------------------|------------------------|------------|
| 12   | Build Wallet Service (7 wallet types) | oenexa-wallet-service  | Week 17–22 |
| 13   | Implement deposit/withdrawal          | oenexa-wallet-service  | Week 19–22 |
| 14   | Implement P2P transfers               | oenexa-wallet-service  | Week 21–24 |
| 15   | Build Payment Service                 | oenexa-payment-service | Week 25–28 |
| 16   | Build Banking Service (ledger)        | oenexa-banking-service | Week 29–32 |
| 17   | Implement fee engine                  | oenexa-payment-service | Week 31–32 |

**Phase 2 Exit Criteria:**
- ✅ All 7 wallet types functional
- ✅ Deposits and withdrawals working
- ✅ P2P transfers with fraud/AML screening
- ✅ Double-entry ledger maintaining consistency
- ✅ Payment gateway integration

### PHASE 3: TRADING PLATFORM (Months 9–12)

| Step | Task                         | Module(s)                  | Duration   |
|------|------------------------------|----------------------------|------------|
| 18   | Build Trading Service (spot) | oenexa-trading-service     | Week 33–38 |
| 19   | Implement margin trading     | oenexa-trading-service     | Week 37–40 |
| 20   | Implement futures trading    | oenexa-trading-service     | Week 39–42 |
| 21   | Build Matching Engine        | oenexa-matching-engine     | Week 43–48 |
| 22   | Build Market Data Service    | oenexa-market-data-service | Week 45–48 |

**Phase 3 Exit Criteria:**
- ✅ All 5 order types (Market, Limit, Stop, Stop-Limit, OCO)
- ✅ Matching engine < 1ms latency
- ✅ WebSocket real-time price feeds
- ✅ 100K+ concurrent orders handled

### PHASE 4: INTELLIGENCE (Months 13–16)

| Step | Task                              | Module(s)               |
|------|-----------------------------------|-------------------------|
| 23   | Build Security Service (fraud ML) | oenexa-security-service |
| 24   | Build Risk Engine (scoring)       | oenexa-risk-engine      |
| 25   | AI Trading Assistant              | oenexa-trading-service  |
| 26   | Copy Trading                      | oenexa-trading-service  |

### PHASE 5: ENTERPRISE (Months 17–20)

| Step | Task                    | Module(s)                |
|------|-------------------------|--------------------------|
| 27   | Build Admin Portal      | oenexa-admin-service     |
| 28   | Build Reporting Service | oenexa-reporting-service |
| 29   | Build Analytics Service | oenexa-analytics-service |
| 30   | Multi-region deployment | Infrastructure           |
| 31   | Load testing (1M users) | All                      |

### PHASE 6: LAUNCH (Months 21–24)

| Step | Task                            |
|------|---------------------------------|
| 32   | Private beta (1,000 testers)    |
| 33   | Public beta (limited countries) |
| 34   | Security audit (3rd party)      |
| 35   | Production launch               |
| 36   | Mobile apps (iOS/Android)       |

---

## 13. Service Dependency Graph

```
                     oenexa-common
                          │
                          ▼
                 oenexa-security-common
                          │
     ┌────────────────────┼────────────────────┐
     │                    │                    │
     ▼                    ▼                    ▼
 API Gateway        All Services        Matching Engine
 (no security-common dependency        (only depends on
  for internal routing only)            oenexa-common)

Inter-Service Runtime Dependencies (via Kafka/REST):

Identity ──→ Notification (email OTP)
Identity ──→ Security (device check)
Identity ──→ Risk Engine (login risk)

KYC ──→ Security (AML screening)
KYC ──→ Wallet (auto-create wallets on KYC approval)

Wallet ──→ Risk Engine (transfer risk)
Wallet ──→ Security (AML screening)
Wallet ──→ Banking (ledger entries)

Trading ──→ Wallet (balance check, fund lock)
Trading ──→ Matching Engine (order submission)

Matching Engine ──→ Wallet (trade settlement)
Matching Engine ──→ Market Data (price updates)

ALL ──→ Notification (event notifications)
ALL ──→ Audit (event logging)
ALL ──→ Analytics (metrics)
```

---

## 14. Port Allocation Map

| Port | Service              | Type                            |
|------|----------------------|---------------------------------|
| 8080 | API Gateway          | Spring Cloud Gateway            |
| 8081 | Identity Service     | Spring Authorization Server     |
| 8082 | User Service         | Spring Boot                     |
| 8083 | KYC Service          | Spring Boot + AI/ML             |
| 8084 | Wallet Service       | Spring Boot                     |
| 8085 | Trading Service      | Spring Boot + WebSocket         |
| 8086 | Matching Engine      | Java (High-Performance)         |
| 8087 | Payment Service      | Spring Boot                     |
| 8088 | Security Service     | Spring Boot + ML                |
| 8089 | Notification Service | Spring Boot + Kafka             |
| 8090 | Reporting Service    | Spring Boot + Elasticsearch     |
| 8091 | Audit Service        | Spring Boot + Kafka             |
| 8092 | Market Data Service  | Spring Boot + WebSocket + Redis |
| 8093 | Admin Service        | Spring Boot                     |
| 8094 | Risk Engine          | Spring Boot + ML                |
| 8095 | Analytics Service    | Spring Boot + Elasticsearch     |
| 8096 | Banking Service      | Spring Boot                     |

---

## 15. Database Allocation Map

| Database          | Service              | Tables Count | Primary Access Pattern                        |
|-------------------|----------------------|--------------|-----------------------------------------------|
| `identity_db`     | Identity Service     | 7            | Heavy read (login), moderate write            |
| `user_db`         | User Service         | 1            | Read-heavy (profile fetch)                    |
| `kyc_db`          | KYC Service          | 3            | Write-moderate (verification flow)            |
| `wallet_db`       | Wallet Service       | 4            | Write-heavy (transactions), requires locking  |
| `trading_db`      | Trading Service      | 5            | Write-heavy (orders), read-heavy (order book) |
| `payment_db`      | Payment Service      | 2            | Write-moderate (payment processing)           |
| `security_db`     | Security Service     | 4            | Write-heavy (event logging)                   |
| `notification_db` | Notification Service | 3            | Write-heavy (notification dispatch)           |
| `audit_db`        | Audit Service        | 2            | Write-heavy (append-only logs)                |
| `banking_db`      | Banking Service      | 4            | Write-heavy (double-entry ledger)             |

---

## 16. Kafka Topic Registry

| Topic                | Partition Count | Retention | Compression |
|----------------------|-----------------|-----------|-------------|
| `user.registered`    | 6               | 30 days   | snappy      |
| `user.login`         | 12              | 7 days    | snappy      |
| `kyc.submitted`      | 6               | 30 days   | snappy      |
| `kyc.approved`       | 6               | 30 days   | snappy      |
| `wallet.deposit`     | 12              | 90 days   | snappy      |
| `wallet.withdrawal`  | 12              | 90 days   | snappy      |
| `transfer.initiated` | 12              | 90 days   | snappy      |
| `transfer.completed` | 12              | 90 days   | snappy      |
| `order.created`      | 24              | 30 days   | lz4         |
| `order.filled`       | 24              | 30 days   | lz4         |
| `trade.executed`     | 24              | 90 days   | lz4         |
| `payment.processed`  | 12              | 90 days   | snappy      |
| `security.alert`     | 6               | 365 days  | snappy      |
| `fraud.detected`     | 6               | 365 days  | snappy      |

---

## 17. Development Environment Setup

### Step-by-Step Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/oenexa/oenexa-platform.git
cd oenexa-platform

# 2. Start infrastructure
docker compose up -d

# 3. Verify infrastructure is healthy
docker compose ps

# 4. Build all modules
./gradlew build -x test

# 5. Run a specific service
./gradlew :oenexa-identity-service:bootRun

# 6. Run all tests
./gradlew test

# 7. View project structure
./gradlew projects
```

### IDE Setup (IntelliJ IDEA)

1. Open as Gradle project
2. Enable annotation processing (Lombok + MapStruct)
3. Set JDK to Java 26
4. Import code style from `.editorconfig`
5. Install Lombok plugin

---

## 18. Coding Standards & Conventions

### Naming Conventions

| Element           | Convention                | Example                          |
|-------------------|---------------------------|----------------------------------|
| Package           | `org.oenexa.{domain}`     | `org.oenexa.identity`            |
| Entity            | `{Name}Entity`            | `UserEntity`                     |
| Repository        | `{Name}Repository`        | `UserRepository`                 |
| Service interface | `{Name}Service`           | `AuthService`                    |
| Service impl      | `{Name}ServiceImpl`       | `AuthServiceImpl`                |
| Controller        | `{Name}Controller`        | `AuthController`                 |
| DTO request       | `{Action}Request`         | `RegisterRequest`                |
| DTO response      | `{Name}Response`          | `AuthResponse`                   |
| Mapper            | `{Name}Mapper`            | `UserMapper`                     |
| Kafka producer    | `{Domain}EventProducer`   | `IdentityEventProducer`          |
| Kafka consumer    | `{Domain}EventConsumer`   | `WalletEventConsumer`            |
| Migration         | `V{n}__{description}.sql` | `V1__create_identity_tables.sql` |

### Annotation Usage

| Annotation           | Usage                                          |
|----------------------|------------------------------------------------|
| `@Data`              | DTOs, non-entity POJOs                         |
| `@Builder`           | Complex DTOs with many fields                  |
| `@Entity` + `@Table` | JPA entities (NOT @Data — use @Getter/@Setter) |
| `@Service`           | Business logic implementations                 |
| `@RestController`    | API endpoints                                  |
| `@Transactional`     | Service methods that modify data               |
| `@Valid`             | Request body validation                        |
| `@Version`           | Optimistic locking fields                      |

---

## 19. Testing Strategy

### BDD & TDD Mandatory Engineering Paradigm

All services and modules across the OENEXA Core platform must be designed, written, and maintained using **TDD (Test-Driven Development)** and **BDD (Behavior-Driven Development)**:

#### 1. Test-Driven Development (TDD) — The Red-Green-Refactor Cycle
1. **Red**: Write a failing unit or behavior test before implementing any feature, bug fix, or business logic.
2. **Green**: Write the minimal code necessary to make the test pass.
3. **Refactor**: Clean up the design, remove code duplication, optimize performance, and keep the test suite green.
4. **Coverage**: 100% code coverage strictly verified via JaCoCo (`minimum = 1.00`).

#### 2. Behavior-Driven Development (BDD) — Given-When-Then Semantics
Every test case must be specified using human-readable domain behaviors:
- **Given**: The preconditions, account balances, user roles, or initial state.
- **When**: The specific action or domain command executed.
- **Then**: The expected observable outcome, balances changed, events produced, or exceptions thrown.

All JUnit 5 test methods must declare `@DisplayName("Given [preconditions], When [action], Then [outcome]")` and delineate sections with `// Given`, `// When`, `// Then`. Go tests must use table-driven subtests with explicit Given-When-Then phases.

#### 3. Strict Native Stub Paradigm & Permanent Mockito Ban
- **Absolute Prohibition of Mockito**: Neither `org.mockito` nor any dynamic bytecode mocking engine is permitted anywhere in the repository.
- **Compile-Time Exclusion**: `org.mockito` is barred in `build.gradle.kts` to guarantee that code using Mockito cannot compile.
- **Native Stubs & Spring Support**: Use decoupled interfaces, native manual fakes/stubs (e.g. `StubPaymentService`, `StubBankingService`), Spring `MockHttpServletRequest`/`MockFilterChain`, and in-memory H2 databases.

### Test Pyramid

```
         ╱╲
        ╱  ╲        E2E Tests (5%)
       ╱    ╲       Full flow testing
      ╱──────╲
     ╱        ╲     Integration Tests (20%)
    ╱          ╲    Testcontainers, Kafka, MySQL
   ╱────────────╲
  ╱              ╲   Unit Tests (75%)
 ╱                ╲  BDD/TDD Service logic, native stubs, mappers, utils
╱──────────────────╲
```

### Testing Tools

| Level       | Tool                                           | Usage                                                      |
|-------------|------------------------------------------------|------------------------------------------------------------|
| BDD / Unit  | JUnit 5 + Spring Test / Native Stubs (No Mockito) | TDD Red-Green-Refactor, Given-When-Then behavioral tests    |
| Coverage    | JaCoCo (`minimum = 1.00`) / `go test`          | Strict 100% verification on all business logic modules     |
| Integration | Testcontainers / Embedded H2                   | Database, Kafka, Redis, in-memory isolation                |
| API         | MockMvc + REST Assured                         | Controller endpoints with HTTP assertion                   |
| Load        | Gatling / k6                                   | Performance benchmarks                                     |
| Security    | OWASP ZAP                                      | Penetration testing                                        |

---

## 20. Deployment Strategy

### Blue-Green Deployment

```
                    Load Balancer
                    ┌─────┐
                    │     │
              ┌─────┴─────┴─────┐
              │                  │
         ┌────▼────┐       ┌────▼────┐
         │  BLUE   │       │  GREEN  │
         │ (Live)  │       │ (New)   │
         │ v1.2.0  │       │ v1.3.0  │
         └─────────┘       └─────────┘
```

1. Deploy new version to GREEN
2. Run smoke tests against GREEN
3. Switch traffic from BLUE → GREEN
4. Monitor for errors
5. If issues → instant rollback to BLUE

---

> **Document End — OENEXA™ Development Process Guide v1.0.0**  
> **© 2026 OENEXA™. All Rights Reserved.**
