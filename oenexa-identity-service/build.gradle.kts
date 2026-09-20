plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":oenexa-common"))
    implementation(project(":oenexa-security-common"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.kafka)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.bouncycastle)
    runtimeOnly(libs.mysql.connector.java)
    
    testImplementation(libs.h2)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mysql)
}

