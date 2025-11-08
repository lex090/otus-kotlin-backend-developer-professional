package com.arbitrage.scanner.app.ktor.koin.modules

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinderParallelImpl
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbOpRepository
import com.arbitrage.scanner.repository.postgres.PostgresArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun blModuleTest(
    initServiceObject: List<CexPrice>,
    initRepoObject: List<CexToCexArbitrageOpportunity>
) = module {
    factory<BusinessLogicProcessorImplDeps> {
        object : BusinessLogicProcessorImplDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
            override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = get()

            override val prodCexPriceClientService: CexPriceClientService = get()
            override val testCexPriceClientService: CexPriceClientService = get()
            override val stubCexPriceClientService: CexPriceClientService = get()

            override val prodArbOpRepository: IArbOpRepository = ArbOpRepositoryInit(
                repo = InMemoryArbOpRepository(),
                initItems = initRepoObject
            )

            override val testArbOpRepository: IArbOpRepository = ArbOpRepositoryInit(
                repo = InMemoryArbOpRepository(),
                initItems = initRepoObject
            )

            override val stubArbOpRepository: IArbOpRepository = InMemoryArbOpRepository()
        }
    }

    factory<CexPriceClientService> {
        object : CexPriceClientService {
            override suspend fun getAllCexPrice(): Set<CexPrice> {
                return initServiceObject.toSet()
            }
        }
    }
    factoryOf(::BusinessLogicProcessorImpl) bind BusinessLogicProcessor::class
    factoryOf(::CexToCexArbitrageFinderParallelImpl) bind CexToCexArbitrageFinder::class
}

/**
 * Тестовый модуль с PostgreSQL репозиторием.
 * Использует реальную БД из TestContainers.
 *
 * @param database Database instance из PostgresTestBase
 * @param initServiceObject Тестовые данные для CexPriceClientService
 * @param initRepoObject Тестовые данные для инициализации репозитория
 */
fun blModuleTestPostgres(
    database: Database,
    initServiceObject: List<CexPrice>,
    initRepoObject: List<CexToCexArbitrageOpportunity>
) = module {
    factory<BusinessLogicProcessorImplDeps> {
        object : BusinessLogicProcessorImplDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
            override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = get()

            override val prodCexPriceClientService: CexPriceClientService = get()
            override val testCexPriceClientService: CexPriceClientService = get()
            override val stubCexPriceClientService: CexPriceClientService = get()

            override val prodArbOpRepository: IArbOpRepository = ArbOpRepositoryInit(
                repo = PostgresArbOpRepository(database),
                initItems = initRepoObject
            )

            override val testArbOpRepository: IArbOpRepository = ArbOpRepositoryInit(
                repo = PostgresArbOpRepository(database),
                initItems = initRepoObject
            )

            override val stubArbOpRepository: IArbOpRepository = PostgresArbOpRepository(database)
        }
    }

    factory<CexPriceClientService> {
        object : CexPriceClientService {
            override suspend fun getAllCexPrice(): Set<CexPrice> {
                return initServiceObject.toSet()
            }
        }
    }
    factoryOf(::BusinessLogicProcessorImpl) bind BusinessLogicProcessor::class
    factoryOf(::CexToCexArbitrageFinderParallelImpl) bind CexToCexArbitrageFinder::class
}