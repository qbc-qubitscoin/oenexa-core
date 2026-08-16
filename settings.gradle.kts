plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "oenexa-core"

// ═══════════════════════════════════════════════════
// OENEXA Core™ — Multi-Module Project Settings
// ═══════════════════════════════════════════════════

// ── Shared Libraries ──
include("oenexa-common")
include("oenexa-security-common")

// ── API Gateway ──
include("oenexa-api-gateway")

// ── Core Services (Phase 1: Foundation) ──
include("oenexa-identity-service")
include("oenexa-user-service")
include("oenexa-kyc-service")

// ── Financial Services (Phase 2: Financial Core) ──
include("oenexa-wallet-service")
include("oenexa-payment-service")
include("oenexa-banking-service")

// ── Trading Services (Phase 3: Trading Platform) ──
// oenexa-trading-service and oenexa-matching-engine are built in Go
include("oenexa-market-data-service")

// ── Security & Intelligence (Phase 4) ──
include("oenexa-security-service")
include("oenexa-risk-engine")

// ── Platform Services ──
include("oenexa-notification-service")
include("oenexa-reporting-service")
include("oenexa-audit-service")
include("oenexa-admin-service")
include("oenexa-analytics-service")
