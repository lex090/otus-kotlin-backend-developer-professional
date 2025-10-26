---

description: "Task list for Recalculate Arbitrage Opportunities feature implementation"
---

# Tasks: Recalculate Arbitrage Opportunities

**Input**: Design documents from `/specs/001-recalculate-arbitrage/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Paths shown assume multi-module Kotlin project structure with composite builds:
- `arbitrage-scanner/arbitrage-scanner-common/src/commonMain/kotlin/...`
- `arbitrage-scanner/arbitrage-scanner-business-logic/src/commonMain/kotlin/...`
- `arbitrage-scanner/arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/...`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create new module and basic project structure

- [ ] T001 Create arbitrage-scanner-repository-inmemory module with build.gradle.kts using BuildPluginMultiplatform
- [ ] T002 Add arbitrage-scanner-repository-inmemory to settings.gradle.kts in arbitrage-scanner/
- [ ] T003 Configure dependencies in arbitrage-scanner-repository-inmemory/build.gradle.kts (common, bignum, coroutines, kotlin-test)
- [ ] T004 Create package structure: arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/
- [ ] T005 Create test package structure: arbitrage-scanner-repository-inmemory/src/commonTest/kotlin/com/arbitrage/scanner/repository/inmemory/
- [ ] T006 Create mocks package: arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/mocks/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core interfaces and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T007 [P] Create CexPriceRepository interface in arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/repository/CexPriceRepository.kt
- [ ] T008 [P] Create ArbitrageOpportunityRepository interface in arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/repository/ArbitrageOpportunityRepository.kt
- [ ] T009 [P] Create ArbitrageFinder interface in arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/services/ArbitrageFinder.kt
- [ ] T010 Extend Context with loadedPrices and foundOpportunities fields in arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/context/Context.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Automatic Recalculation Trigger (Priority: P1) 🎯 MVP

**Goal**: Реализовать базовый механизм пересчёта арбитражных возможностей при получении события RECALCULATE

**Independent Test**: Отправить событие RECALCULATE, проверить что система возвращает список найденных возможностей с корректными данными

### Implementation for User Story 1

- [ ] T011 [P] [US1] Implement InMemoryCexPriceRepository in arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryCexPriceRepository.kt
- [ ] T012 [P] [US1] Implement InMemoryArbitrageOpportunityRepository in arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryArbitrageOpportunityRepository.kt
- [ ] T013 [P] [US1] Create MockCexPriceGenerator with Config data class in arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/mocks/MockCexPriceGenerator.kt
- [ ] T014 [US1] Implement ArbitrageFinderImpl service in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/services/ArbitrageFinderImpl.kt
- [ ] T015 [US1] Create LoadCexPricesWorker in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/workers/recalculate/LoadCexPricesWorker.kt
- [ ] T016 [US1] Create FindArbitrageOpportunitiesWorker in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/workers/recalculate/FindArbitrageOpportunitiesWorker.kt
- [ ] T017 [US1] Create SaveOpportunitiesWorker in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/workers/recalculate/SaveOpportunitiesWorker.kt
- [ ] T017a [US1] Add timestamp generation for startTimestamp in SaveOpportunitiesWorker.kt
- [ ] T018 [US1] Create PrepareRecalculateResponseWorker in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/workers/recalculate/PrepareRecalculateResponseWorker.kt
- [ ] T019 [US1] Integrate RECALCULATE workers chain into BusinessLogicProcessorImpl in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/BusinessLogicProcessorImpl.kt
- [ ] T020 [US1] Update ArbOpStubs with mock CexPrice data in arbitrage-scanner-stubs/src/commonMain/kotlin/com/arbitrage/scanner/ArbOpStubs.kt
- [ ] T021 [US1] Add logging for recalculate operations using arbitrage-scanner-lib-logging in workers

### Tests for User Story 1

- [ ] T022 [P] [US1] Unit test for InMemoryCexPriceRepository CRUD operations in arbitrage-scanner-repository-inmemory/src/commonTest/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryCexPriceRepositoryTest.kt
- [ ] T023 [P] [US1] Unit test for InMemoryArbitrageOpportunityRepository CRUD operations in arbitrage-scanner-repository-inmemory/src/commonTest/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryArbitrageOpportunityRepositoryTest.kt
- [ ] T024 [P] [US1] Unit test for MockCexPriceGenerator determinism and data characteristics in arbitrage-scanner-repository-inmemory/src/commonTest/kotlin/com/arbitrage/scanner/repository/inmemory/mocks/MockCexPriceGeneratorTest.kt
- [ ] T025 [P] [US1] Unit test for ArbitrageFinderImpl algorithm correctness in arbitrage-scanner-business-logic/src/commonTest/kotlin/com/arbitrage/scanner/services/ArbitrageFinderImplTest.kt
- [ ] T026 [US1] Integration test for full RECALCULATE flow in arbitrage-scanner-business-logic/src/commonTest/kotlin/com/arbitrage/scanner/BusinessLogicProcessorImplRecalculateTest.kt
- [ ] T027 [US1] Test edge cases: empty prices, single exchange, zero spread in BusinessLogicProcessorImplRecalculateTest.kt

**Checkpoint**: User Story 1 is fully functional and independently testable. System can trigger recalculation and find arbitrage opportunities.

---

## Phase 4: User Story 2 - Optimized Large Dataset Processing (Priority: P2)

**Goal**: Оптимизировать алгоритм для быстрой обработки больших объёмов данных (1000+ записей < 1 сек)

**Independent Test**: Генерировать 1000 ценовых записей, измерить время выполнения, проверить что < 1 секунды

### Implementation for User Story 2

- [ ] T028 [US2] Optimize ArbitrageFinderImpl: use sequences instead of lists where applicable in arbitrage-scanner-business-logic/src/commonMain/kotlin/com/arbitrage/scanner/services/ArbitrageFinderImpl.kt
- [ ] T029 [US2] Add sorting by spread (descending) to findOpportunities result in ArbitrageFinderImpl.kt
- [ ] T030 [US2] Optimize MockCexPriceGenerator for large datasets (minimize allocations) in MockCexPriceGenerator.kt
- [ ] T031 [US2] Add configuration support for large dataset generation (100 tokens, 10 exchanges) in MockCexPriceGenerator.kt

### Tests for User Story 2

- [ ] T032 [P] [US2] Performance test: 1000 records < 1 second in arbitrage-scanner-business-logic/src/commonTest/kotlin/com/arbitrage/scanner/services/ArbitrageFinderPerformanceTest.kt
- [ ] T033 [P] [US2] Test sorting: opportunities sorted by spread descending in ArbitrageFinderImplTest.kt
- [ ] T034 [US2] Integration test: large dataset recalculate performance in BusinessLogicProcessorImplRecalculateTest.kt

**Checkpoint**: User Story 2 is functional. System efficiently processes large datasets with target performance met.

---

## Phase 5: User Story 3 - In-Memory Repository Storage (Priority: P3)

**Goal**: Обеспечить корректное сохранение и доступ к найденным возможностям через READ и SEARCH

**Independent Test**: Выполнить RECALCULATE, затем READ/SEARCH для проверки доступности данных

### Implementation for User Story 3

- [ ] T035 [US3] Implement markAsEnded logic in SaveOpportunitiesWorker: mark old opportunities as ended before saving new in SaveOpportunitiesWorker.kt
- [ ] T037 [US3] Implement auto-ID generation in InMemoryArbitrageOpportunityRepository.kt using AtomicLong
- [ ] T038 [US3] Add thread-safety validation for concurrent access to repositories

### Tests for User Story 3

- [ ] T039 [P] [US3] Test markAsEnded: old opportunities get endTimestamp set in InMemoryArbitrageOpportunityRepositoryTest.kt
- [ ] T040 [P] [US3] Test auto-ID generation: sequential IDs for new opportunities in InMemoryArbitrageOpportunityRepositoryTest.kt
- [ ] T041 [P] [US3] Test findActive: returns only opportunities with endTimestamp == null in InMemoryArbitrageOpportunityRepositoryTest.kt
- [ ] T042 [US3] Integration test: recalculate → READ → verify data correctness in BusinessLogicProcessorImplRecalculateTest.kt
- [ ] T043 [US3] Integration test: recalculate → SEARCH with filter → verify results in BusinessLogicProcessorImplRecalculateTest.kt
- [ ] T044 [US3] Test thread safety: concurrent recalculate and read operations in BusinessLogicProcessorImplRecalculateTest.kt

**Checkpoint**: All user stories are independently functional. Complete end-to-end flow working with persistence.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T045 [P] Add KDoc documentation to ArbitrageFinderImpl algorithm explanation
- [ ] T046 [P] Add KDoc documentation to repository implementations
- [ ] T047 [P] Verify all logging statements include requestId context
- [ ] T048 Run multiplatform tests on all targets: jvmTest, linuxX64Test, macosArm64Test
- [ ] T049 Run performance benchmarks and document results in quickstart.md
- [ ] T050 Code review: verify compliance with Constitution principles (simplicity, testability, performance)
- [ ] T051 Update CLAUDE.md with new modules and patterns if needed
- [ ] T052 Run ./gradlew :arbitrage-scanner:checkAll and fix any issues

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phases 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Optimizes US1 but independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Extends US1 persistence but independently testable

### Within Each User Story

**User Story 1** task flow:
1. T011, T012, T013 (repositories & mock generator) - can run in parallel [P]
2. T014 (ArbitrageFinder) - depends on having mockable data structure
3. T015, T016, T017, T017a, T018 (workers) - sequential, T015 → T016 → T017 → T017a → T018
4. T019 (integration into processor) - depends on all workers
5. T020, T021 (stubs & logging) - can run in parallel [P] after integration
6. T022-T027 (tests) - T022-T025 can run in parallel [P], T026-T027 sequential

**User Story 2** task flow:
1. T028, T029 (optimization) - sequential (same file)
2. T030, T031 (mock optimization) - sequential (same file)
3. T032, T033 (tests) - can run in parallel [P]
4. T034 (integration test) - after all implementation

**User Story 3** task flow:
1. T035, T037, T038 (implementation) - sequential (builds on each other)
2. T039, T040, T041 (repository tests) - can run in parallel [P]
3. T042, T043, T044 (integration tests) - sequential

### Parallel Opportunities

- **Setup phase**: T001-T006 can run sequentially (module creation)
- **Foundational phase**: T007, T008, T009 can run in parallel [P]
- **User Story 1 implementation**: T011, T012, T013 in parallel; T020, T021 in parallel
- **User Story 1 tests**: T022, T023, T024, T025 in parallel
- **User Story 2 tests**: T032, T033 in parallel
- **User Story 3 tests**: T039, T040, T041 in parallel
- **Polish phase**: T045, T046, T047 in parallel

---

## Parallel Example: User Story 1

```bash
# Launch repositories and mock generator together:
Task: "T011 [P] [US1] Implement InMemoryCexPriceRepository..."
Task: "T012 [P] [US1] Implement InMemoryArbitrageOpportunityRepository..."
Task: "T013 [P] [US1] Create MockCexPriceGenerator..."

# Launch unit tests together:
Task: "T022 [P] [US1] Unit test for InMemoryCexPriceRepository..."
Task: "T023 [P] [US1] Unit test for InMemoryArbitrageOpportunityRepository..."
Task: "T024 [P] [US1] Unit test for MockCexPriceGenerator..."
Task: "T025 [P] [US1] Unit test for ArbitrageFinderImpl..."
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T006)
2. Complete Phase 2: Foundational (T007-T010) - CRITICAL blocking phase
3. Complete Phase 3: User Story 1 (T011-T021, T022-T027 tests)
4. **STOP and VALIDATE**: Test User Story 1 independently
   - Trigger RECALCULATE with mock data
   - Verify opportunities are found
   - Validate edge cases
5. Deploy/demo if ready - **this is a complete, valuable increment**

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP! ✅)
3. Add User Story 2 → Test independently → Deploy/Demo (Performance optimization)
4. Add User Story 3 → Test independently → Deploy/Demo (Full persistence)
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (blocking work)
2. Once Foundational is done:
   - **Developer A**: User Story 1 (T011-T021 + T022-T027 tests) - MVP implementation
   - **Developer B**: User Story 2 (T028-T034) - Performance optimization (can start in parallel after foundation)
   - **Developer C**: User Story 3 (T035, T037-T044) - Persistence enhancement (can start in parallel after foundation)
3. Stories complete and integrate independently
4. Each developer validates their story independently before integration

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Run `./gradlew :arbitrage-scanner:checkAll` before committing
- For multiplatform tests: `./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:allTests`
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Summary

- **Total Tasks**: 52
- **Setup Phase**: 6 tasks
- **Foundational Phase**: 4 tasks (BLOCKING)
- **User Story 1 (P1)**: 18 tasks (12 implementation + 6 tests)
- **User Story 2 (P2)**: 7 tasks (4 implementation + 3 tests)
- **User Story 3 (P3)**: 9 tasks (3 implementation + 6 tests)
- **Polish Phase**: 8 tasks

**Parallel Opportunities**: 15+ tasks can run in parallel across phases

**MVP Scope**: Phase 1 + Phase 2 + Phase 3 (User Story 1) = 28 tasks

**Estimated Delivery**:
- MVP (US1): ~2-3 days for single developer
- + Performance (US2): +1 day
- + Full feature (US3): +1-2 days
- **Total**: 4-6 days for complete feature
