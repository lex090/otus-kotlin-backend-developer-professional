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
            override val testCexPriceClientService: CexPriceClientService = get<CexPriceClientServiceTest>()
            override val testArbOpRepository: IArbOpRepository = get<InMemoryArbOpRepository>()
        }
    }
    factory<BusinessLogicProcessor> { BusinessLogicProcessorImpl(get()) }
    factory<CexPriceClientServiceTest> { CexPriceClientServiceTest() }
    factory<CexToCexArbitrageFinder> { CexToCexArbitrageFinderParallelImpl() }
    single<InMemoryArbOpRepository> { InMemoryArbOpRepository() }
}

val loggingModule = module {
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}