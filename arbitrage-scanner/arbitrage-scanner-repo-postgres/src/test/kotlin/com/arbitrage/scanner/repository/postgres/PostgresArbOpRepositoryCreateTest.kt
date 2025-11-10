package com.arbitrage.scanner.repository.postgres

import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpCreateTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Тесты операций создания для PostgreSQL репозитория.
 * Наследует все тесты из базового класса RepositoryArbOpCreateTest.
 */
@Testcontainers
class PostgresArbOpRepositoryCreateTest : RepositoryArbOpCreateTest() {
    companion object {
        private val db = PostgresTestBase
    }

    @BeforeEach
    fun setUp() = runTest {
        db.clearDatabase()
    }

    override fun createRepository(): IArbOpRepository {
        return PostgresArbOpRepository(db.database)
    }
}
