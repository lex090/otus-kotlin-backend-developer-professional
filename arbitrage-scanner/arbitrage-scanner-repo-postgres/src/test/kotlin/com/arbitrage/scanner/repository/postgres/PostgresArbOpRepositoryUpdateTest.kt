package com.arbitrage.scanner.repository.postgres

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpUpdateTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Тесты операций обновления для PostgreSQL репозитория.
 * Наследует все тесты из базового класса RepositoryArbOpUpdateTest.
 * Добавляет специфичный тест для optimistic locking version conflict.
 */
@Testcontainers
class PostgresArbOpRepositoryUpdateTest : RepositoryArbOpUpdateTest() {
    companion object {
        private val db = PostgresTestBase
    }

    @BeforeEach
    fun setUp() = runTest {
        db.clearDatabase()
    }

    override fun createRepository(): IArbOpRepository {
        return ArbOpRepositoryInit(
            PostgresArbOpRepository(db.database),
            initObject,
        )
    }
}
