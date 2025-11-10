package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.app.ktor.koin.modules.blModuleTestPostgres
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.koin.core.module.Module
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Интеграционные тесты Ktor приложения с PostgreSQL репозиторием.
 * Использует TestContainers для запуска реального PostgreSQL.
 */
@Testcontainers
class KtorApplicationPostgresRepoTest : KtorApplicationBaseTest() {
    companion object {
        private val db = PostgresTestBase
    }

    override val workMode: ArbitrageOpportunityRequestDebugModeApi = ArbitrageOpportunityRequestDebugModeApi.PROD

    @BeforeEach
    fun setUp() = runTest {
        db.clearDatabase()
    }

    override val readModule: Module = blModuleTestPostgres(
        database = db.database,
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )

    override val searchModule: Module = blModuleTestPostgres(
        database = db.database,
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )

    override val recalculateModule: Module = blModuleTestPostgres(
        database = db.database,
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )
}
