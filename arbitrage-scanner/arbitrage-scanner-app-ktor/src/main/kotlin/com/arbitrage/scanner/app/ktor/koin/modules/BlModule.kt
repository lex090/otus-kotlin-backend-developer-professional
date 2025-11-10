package com.arbitrage.scanner.app.ktor.koin.modules

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinderParallelImpl
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService
import com.arbitrage.scanner.service.CexPriceClientServiceStub
import com.arbitrage.scanner.service.CexPriceClientServiceTest
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val blModule = module {
    factory<BusinessLogicProcessorImplDeps> {
        object : BusinessLogicProcessorImplDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
            override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = get()

            // TODO Позже поменять
            override val prodCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceTest>()
            override val testCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceTest>()
            override val stubCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceStub>()

            // Production использует PostgreSQL репозиторий
            override val prodArbOpRepository: IArbOpRepository = get(named("postgres"))

            // Test и Stub остаются InMemory для быстрых тестов
            override val testArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>()
            override val stubArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>()
        }
    }
    factoryOf(::BusinessLogicProcessorImpl) bind BusinessLogicProcessor::class
    factoryOf(::CexPriceClientServiceTest)
    factoryOf(::CexPriceClientServiceStub)
    factoryOf(::CexToCexArbitrageFinderParallelImpl) bind CexToCexArbitrageFinder::class
    single<InMemoryArbOpRepository> { InMemoryArbOpRepository() }
}