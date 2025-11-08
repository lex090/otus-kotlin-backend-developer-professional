package com.arbitrage.scanner.repository.postgres

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpReadTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Тесты операций чтения для PostgreSQL репозитория.
 * Наследует все тесты из базового класса RepositoryArbOpReadTest.
 */
@Testcontainers
class PostgresArbOpRepositoryReadTest : RepositoryArbOpReadTest() {
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
