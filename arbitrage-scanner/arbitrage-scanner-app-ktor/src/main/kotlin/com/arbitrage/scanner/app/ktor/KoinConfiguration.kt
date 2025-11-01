package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinderParallelImpl
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService
import com.arbitrage.scanner.service.CexPriceClientServiceStub
import com.arbitrage.scanner.service.CexPriceClientServiceTest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            jsonModule,
            businessLogicProcessorModule,
            loggingModule,
        )
    }
}

val jsonModule = module {
    factory<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

val businessLogicProcessorModule = module {
    factory<BusinessLogicProcessorImplDeps> {
        object : BusinessLogicProcessorImplDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
            override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = get()
            override val prodCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceStub>() // TODO Позже поменять
            override val testCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceTest>()
            override val stubCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceStub>()
            override val prodArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>() // TODO Позже поменять
            override val testArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>()
            override val stubArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>()
        }
    }
    factory<BusinessLogicProcessor> { BusinessLogicProcessorImpl(get()) }
    factory<CexPriceClientServiceTest> { CexPriceClientServiceTest() }
    factory<CexPriceClientServiceStub> { CexPriceClientServiceStub() }
    factory<CexToCexArbitrageFinder> { CexToCexArbitrageFinderParallelImpl() }
    single<InMemoryArbOpRepository> { InMemoryArbOpRepository() }
}

val loggingModule = module {
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}