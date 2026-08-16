# OENEXA Core™ Backend Platform (`oenexa-core`)

[![Backend CI/CD Pipeline](https://github.com/qbc-qubitscoin/oenexa-core/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/qbc-qubitscoin/oenexa-core/actions/workflows/backend-ci.yml)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![Go](https://img.shields.io/badge/Go-1.26-blue.svg)](https://golang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

**OENEXA Core** (`oenexa-core`) is the enterprise-grade, high-performance hybrid-architecture cryptocurrency exchange and trading backend platform. It powers the core orderbook matching engine, real-time trading gateway, multi-currency ledger/wallet systems, and automated compliance and risk pipelines.

> ℹ️ **Frontend Architecture**: The Web UI has been decoupled into its own independent domain-driven repository at [`qbc-qubitscoin/oenexa-ui`](https://github.com/qbc-qubitscoin/oenexa-ui).

---

## 🏗️ Architecture Overview

The platform uses a polyglot microservice architecture designed for ultra-low latency execution and strict financial consistency:

- **Order Matching & Trading Core (Go 1.26)**: In-memory orderbook matching engine and high-throughput WebSocket API gateway with Kafka event pipelining.
- **Financial & Regulatory Engines (Java 25 / Spring Boot 4.1.0)**: ACID transactional double-entry ledger, multi-currency wallets, KYC verification, payment gateway integrations, and bank reconciliations.
- **Database per Service Pattern**: Independent schemas provisioned in MySQL 9 with Flyway migrations, Redis 8 for low-latency session and rate limiting, and Apache Kafka for event-driven messaging.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY LAYER (:8080)                            │
│  Rate Limiting  •  JWT Verification  •  Route Mapping  •  Resilience4j  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     MICROSERVICES LAYER                                  │
│                                                                         │
│  Identity (:8081)    • User (:8082)         • KYC/AML (:8083)           │
│  Wallet (:8084)      • Trading Engine (:8085 Go)                        │
│  Matching (:8086 Go) • Payment (:8087)      • Security & Fraud (:8088)  │
│  Notification (:8089)• Reporting (:8090)    • Audit (:8091)             │
│  Market Data (:8092) • Admin Portal (:8093) • Risk Engine (:8094)       │
│  Analytics (:8095)   • Banking (:8096)                                  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        DATA & MESSAGING LAYER                           │
│  MySQL 9 (10 Isolated DBs) • Redis 8 • Apache Kafka • MinIO S3 • Elastic│
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Microservice Module Registry

| Service Module | Tech Stack | Port | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| `oenexa-common` | Java 25 | N/A | Shared DTOs, domain models, exceptions, and utility classes |
| `oenexa-security-common` | Java 25 | N/A | Shared JWT filters, token providers, and security contexts |
| `oenexa-api-gateway` | Spring Cloud | 8080 | Central API entry point, routing, and rate limiting |
| `oenexa-identity-service` | Spring Boot | 8081 | Authentication, OAuth2/OIDC, MFA, and credential issuing |
| `oenexa-user-service` | Spring Boot | 8082 | User profiles, accounts, and preferences management |
| `oenexa-kyc-service` | Spring Boot | 8083 | Identity verification, document intake, and AML checks |
| `oenexa-wallet-service` | Spring Boot | 8084 | Multi-currency balances, deposits, withdrawals, and locks |
| `oenexa-trading-service` | Go 1.26 | 8085 | Trading REST endpoints, WebSocket live hub, order submission |
| `oenexa-matching-engine` | Go 1.26 | 8086 | Ultra-fast in-memory orderbook matching & trade generation |
| `oenexa-payment-service` | Spring Boot | 8087 | Fiat payment rails, credit card, and crypto on-ramps |
| `oenexa-security-service` | Spring Boot | 8088 | Real-time fraud detection, IP intelligence, and device auditing |
| `oenexa-notification-service` | Spring Boot | 8089 | Transactional email, SMS, and push notification dispatch |
| `oenexa-reporting-service` | Spring Boot | 8090 | Financial ledgers, compliance tax reports, and statements |
| `oenexa-audit-service` | Spring Boot | 8091 | Immutable append-only audit trails for regulatory compliance |
| `oenexa-market-data-service` | Spring Boot | 8092 | Candlesticks, ticker aggregation, and historic trade archives |
| `oenexa-admin-service` | Spring Boot | 8093 | Administrative dashboard and operational controls |
| `oenexa-risk-engine` | Spring Boot | 8094 | Pre-trade and post-trade risk evaluations and limits |
| `oenexa-analytics-service` | Spring Boot | 8095 | User engagement and platform telemetry analysis |
| `oenexa-banking-service` | Spring Boot | 8096 | Direct banking integration, SEPA/ACH wires, and settlements |

---

## 🛠️ Step-by-Step Development Process & Design

For deep architectural blueprints, database schema specifications, Kafka topic catalogs, and design patterns, consult:
- **[OENEXA Core Development Process Guide](OENEXA_Development_Process.md)**

---

## 🚀 How to Run Locally

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (running)
- **Java 25** (Eclipse Temurin or OpenJDK 25)
- **Go 1.22+** (Go 1.26 recommended)

### 1. Infrastructure Boot (Docker Compose)
Start the foundational databases, cache, and Kafka messaging services:
```bash
docker compose up -d
```
This automatically initiates:
- **MySQL 9** (Port 3307, configurable via `MYSQL_PORT`) with all 10 databases initialized via `scripts/init-databases.sql`
- **Redis 8** (Port 6379)
- **Kafka & Zookeeper** (Ports 9092, 2181)
- **Kafka UI** (Port 8180 - open `http://localhost:8180` to inspect topics)
- **Elasticsearch 8** (Port 9200)
- **MinIO S3** (Ports 9000, 9001)

### 2. Run All Core Services (One-Click)
Use the automated launcher scripts:
- **Windows (PowerShell):**
  ```powershell
  .\run-all.ps1
  ```
- **Linux / macOS:**
  ```bash
  chmod +x run-all.sh
  ./run-all.sh
  ```

This starts the Docker infrastructure, compiles and runs the Java Wallet Service, boots the Go Trading Service (with WebSocket hub on `:8084`), launches the Go Matching Engine, and connects to `../oenexa-ui` if present.

### 3. Running Automated Tests
Run unit, integration, and Cucumber BDD test suites across all 17 submodules:
- **Windows:**
  ```powershell
  .\run-test.ps1
  ```
- **Linux / macOS:**
  ```bash
  ./run-test.sh
  ```
Or directly via Gradle:
```bash
./gradlew test
```

---

## 🚢 Production Deployment & CI/CD

- **GitHub Actions**: Fully automated multi-stage CI/CD pipeline defined in [`.github/workflows/backend-ci.yml`](.github/workflows/backend-ci.yml).
  - Code Quality & Security: Go vet/fmt, Gosec scanner, Java test suites.
  - Containerization: Builds and publishes OCI container images to GitHub Container Registry (`ghcr.io/qbc-qubitscoin/oenexa-core/*`).
  - Automated VPS Delivery: Secure deployment via SCP/SSH orchestrating `docker-compose.prod.yml`.
- **Kubernetes**: Production-ready declarative K8s manifests with Kustomize overlays in [`kubernetes/`](kubernetes/) for `dev`, `staging`, and `production`.
