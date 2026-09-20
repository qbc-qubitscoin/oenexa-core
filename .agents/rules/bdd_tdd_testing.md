---
description: Enforces strict BDD (Behavior-Driven Development) and TDD (Test-Driven Development) testing methodologies for all tests across the OENEXA Core platform.
---

# BDD & TDD Testing Mandatory Rules

All code in the **OENEXA Core** (`oenexa-core`) platform must be designed, implemented, and verified using **TDD (Test-Driven Development)** and **BDD (Behavior-Driven Development)**.

---

## 1. Test-Driven Development (TDD) Mandate

Every feature, bug fix, validation rule, business process, and service enhancement MUST follow the **TDD Red-Green-Refactor** cycle:

1. **🔴 Red Phase (Test First)**:
   - Write unit or behavior test(s) **before** or concurrently with implementation logic.
   - The test must define the expected behavior, input constraints, calculations, and boundaries.
   - Confirm the test fails for the expected reason before writing production code.

2. **🟢 Green Phase (Minimal Implementation)**:
   - Implement only the minimal necessary production code to make the test pass cleanly.
   - Avoid speculative or dead code.

3. **🔵 Refactor Phase (Clean Architecture)**:
   - Refactor code to adhere to DRY (Don't Repeat Yourself), SOLID principles, and clean domain design.
   - Optimize performance without changing observable behavior.
   - Verify that all tests remain green and JaCoCo coverage remains at **100%**.

---

## 2. Behavior-Driven Development (BDD) Specification

All tests must be written from the perspective of **system behavior** and **domain expectations**, using explicit **Given - When - Then** semantics:

1. **Given (Preconditions / Context)**:
   - The initial state of the domain, accounts, orders, balances, or configuration before the action occurs.
   - Setup mocks, stubs, or test fixture models.

2. **When (Action / Event Trigger)**:
   - The specific operation invoked (e.g., submitting an order, executing a deposit, validating a JWT, matching a trade).

3. **Then (Outcome / Invariants)**:
   - The expected observable outcome, return value, state transitions, events published to Kafka, or exceptions thrown.

---

## 3. Implementation Standards by Language

### Java (JUnit 5 + Spring Boot)
- **BDD Display Names**: Every test method must be annotated with `@DisplayName`:
  ```java
  @Test
  @DisplayName("Given valid deposit request, When processDeposit is executed, Then balance is incremented and transaction recorded")
  void givenValidDeposit_whenProcessDeposit_thenIncrementBalance() {
      // Given (Arrange)
      DepositRequest request = new DepositRequest(100L, new BigDecimal("250.00"), "USD");

      // When (Act)
      DepositResponse response = bankingService.processDeposit(request);

      // Then (Assert)
      assertNotNull(response);
      assertEquals(new BigDecimal("250.00"), response.getNewBalance());
  }
  ```
- **Structure**: Organize test blocks with explicit comments (`// Given`, `// When`, `// Then`) for clear readability.
- **Exceptions**: Use `assertThrows` to verify domain boundary violations and error handling.

### Go (Trading & Matching Microservices)
- Use table-driven test cases or subtests (`t.Run`) structured with explicit Given-When-Then phases:
  ```go
  t.Run("Given ask order at 50000, When taker buy order at 50000 arrives, Then orderbook matches and executes trade", func(t *testing.T) {
      // Given: orderbook with existing maker sell order
      ob := NewOrderBook("BTC-USD")
      ob.AddOrder(makerSellOrder)

      // When: taker buy order arrives
      ob.ProcessOrder(takerBuyOrder)

      // Then: trade is executed and maker order removed
      assert.Equal(t, 0, len(ob.Asks))
  })
  ```

---

## 4. Coverage & Verification Invariants

- **100% JaCoCo Verification**: All Java modules must pass `./gradlew test jacocoTestReport jacocoTestCoverageVerification` with `minimum = 1.00`.
- **Zero Untested Logic**: No controller, service, domain mapper, validator, or math algorithm may exist without corresponding BDD/TDD tests.
- **Fail Fast & Deterministic**: Tests must be hermetic and execute quickly without relying on external shared infrastructure.
