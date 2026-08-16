#!/bin/bash

echo "========================================="
echo "Running BDD/TDD Tests & JaCoCo Coverage for OENEXA Core..."
echo "========================================="

# Ensure gradlew has execute permission
chmod +x ./gradlew

# 1. Java Services BDD/TDD & JaCoCo 100% Verification
echo "Running Java Multi-Module Test Suites & JaCoCo Coverage Verification..."
./gradlew test jacocoTestReport jacocoTestCoverageVerification
if [ $? -ne 0 ]; then
    echo "❌ Java test or coverage verification failed."
    exit 1
fi

# 2. Go Matching Engine
echo "Running Go Matching Engine BDD/TDD Tests..."
(cd oenexa-matching-engine && go test -v -cover ./...)
if [ $? -ne 0 ]; then
    echo "❌ Go Matching Engine tests failed."
    exit 1
fi

# 3. Go Trading Service
echo "Running Go Trading Service BDD/TDD Tests..."
(cd oenexa-trading-service && go test -v -cover ./...)
if [ $? -ne 0 ]; then
    echo "❌ Go Trading Service tests failed."
    exit 1
fi

echo "========================================="
echo "✅ All BDD/TDD tests & JaCoCo coverage verified successfully!"
echo "========================================="
