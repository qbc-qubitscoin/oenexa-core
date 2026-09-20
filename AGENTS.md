# OENEXA Core™ — Agent Rules & Engineering Standards

## 1. Mandatory BDD & TDD Testing Paradigm
All testing across OENEXA Core must strictly follow **Behavior-Driven Development (BDD)** and **Test-Driven Development (TDD)**:
- **TDD (Red-Green-Refactor)**:
  - Tests MUST be written before or alongside implementation.
  - Never introduce new business logic, domain services, or endpoints without writing failing tests first.
  - Implement the minimal correct code to satisfy the test, then refactor.
- **BDD (Given - When - Then)**:
  - All test methods must be structured using explicit **Given - When - Then** semantics.
  - Test naming must describe human-readable behavior using `@DisplayName("Given [precondition], When [action], Then [outcome]")` in JUnit 5, and clear scenario descriptions in Go.
  - In-code comments must clearly delimit `// Given`, `// When`, `// Then`.
- **100% Code Coverage**:
  - Unit tests MUST achieve 100% test coverage.
  - JaCoCo coverage verification (`limit { minimum = 1.00 }`) is enforced on `./gradlew test jacocoTestReport jacocoTestCoverageVerification`.
  - Zero untested logic paths, error handling blocks, or domain validation checks.

## 2. Tech Stack & Version Invariants
- **Java**: JDK 25 LTS.
- **Spring Boot**: Spring Boot 4.1.0 (or latest compatible 3.4+ LTS).
- **Go**: Go 1.26.
- **Node.js**: Node 24 LTS.
- NEVER downgrade these versions to resolve build or runtime issues.

## 3. Code Originality & Architecture
- Prioritize native, custom-built logic perfectly tailored to the framework.
- Adhere strictly to DRY (Don't Repeat Yourself) and SOLID principles.
- Use native Spring capabilities, manual stubs/fakes, or in-memory databases (H2) for testing rather than heavy third-party mock engines.
- Maintain decoupled interfaces (`OrderProducer`, `MessageWriter`) to keep unit test suites hermetic and reliable.

## 4. Strict Prohibition of Mockito & Dynamic Mock Frameworks
- **NEVER use Mockito (`org.mockito`) or any third-party dynamic mock engine.**
- All tests MUST use native custom stubs/fakes, real domain implementations, in-memory fixtures (H2, real collections), or native Spring test support classes (`MockHttpServletRequest`, `MockHttpServletResponse`, `MockFilterChain`, `SecurityContextHolder`).
- Any import of `org.mockito.*`, annotations (`@Mock`, `@Spy`, `@InjectMocks`), or methods (`mock()`, `when()`, `verify()`) is strictly prohibited and will be rejected.
- Mockito dependencies are explicitly excluded at the build level (`build.gradle.kts`). Agents must NEVER re-add or bypass this exclusion.
