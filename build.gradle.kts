// ═══════════════════════════════════════════════════
// OENEXA™ — Root Build Configuration
// ═══════════════════════════════════════════════════

plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    id("org.sonarqube") version "6.0.1.5171"
}

group = "com.oenexa"
version = "0.0.1-SNAPSHOT"
description = "OENEXA™ — Open Economy Next Generation Exchange & Assets"

// ── Common configuration for ALL subprojects ──
subprojects {
    plugins.apply("java")
    plugins.apply("jacoco")
    plugins.apply("io.spring.dependency-management")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {

        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${rootProject.libs.versions.spring.boot.get()}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${rootProject.libs.versions.spring.cloud.get()}")
        }
    }

    configurations.all {
        exclude(group = "org.mockito")
        exclude(module = "mockito-core")
        exclude(module = "mockito-junit-jupiter")
    }

    dependencies {
        // Lombok — available to all modules
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        testCompileOnly(rootProject.libs.lombok)
        testAnnotationProcessor(rootProject.libs.lombok)

        // MapStruct
        implementation(rootProject.libs.mapstruct)
        annotationProcessor(rootProject.libs.mapstruct.processor)

        // Testing
        // Testing - TDD Framework (JUnit 5) and Native Testing (H2) - STRICT ZERO MOCKITO ENFORCEMENT
        testImplementation(rootProject.libs.spring.boot.starter.test) {
            exclude(group = "org.mockito")
        }
        testImplementation("com.h2database:h2")
        
        // Testing - BDD Framework (Cucumber)
        testImplementation("io.cucumber:cucumber-java:7.21.1")
        testImplementation("io.cucumber:cucumber-spring:7.21.1")
        testImplementation("io.cucumber:cucumber-junit-platform-engine:7.21.1")
        testImplementation("org.junit.platform:junit-platform-suite")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs(
            "--sun-misc-unsafe-memory-access=allow"
        )
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters"))
        options.isFork = true
        options.forkOptions.jvmArgs = listOf(
            "--sun-misc-unsafe-memory-access=allow",
            "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED"
        )
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        afterEvaluate {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/dto/**",
                        "**/entity/**",
                        "**/model/**",
                        "**/constant/**",
                        "**/event/**",
                        "**/config/**",
                        "**/*SecurityConfig*",
                        "**/*JwtAuthenticationFilter*",
                        "**/kafka/**"
                    )
                }
            }))
        }
    }

    tasks.withType<JacocoCoverageVerification> {
        onlyIf {
            val testTask = tasks.findByName("test") as? Test
            val filter = testTask?.filter
            val isFiltered = filter?.let {
                val method = it.javaClass.methods.find { m -> m.name == "getCommandLineIncludePatterns" }
                val patterns = method?.invoke(it) as? Set<*>
                patterns?.isNotEmpty() == true
            } ?: false
            !isFiltered
        }
        violationRules {
            rule {
                limit {
                    minimum = 1.00.toBigDecimal()
                }
            }
        }
        afterEvaluate {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/dto/**",
                        "**/entity/**",
                        "**/model/**",
                        "**/constant/**",
                        "**/event/**",
                        "**/config/**",
                        "**/*SecurityConfig*",
                        "**/*JwtAuthenticationFilter*",
                        "**/kafka/**"
                    )
                }
            }))
        }
    }

    tasks.withType<Test> {
        finalizedBy("jacocoTestReport", "jacocoTestCoverageVerification")
    }
}
