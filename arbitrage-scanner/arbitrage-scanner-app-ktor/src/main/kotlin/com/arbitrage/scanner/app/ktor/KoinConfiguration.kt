package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import com.arbitrage.scanner.repository.ArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.CexPriceRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryCexPriceRepository
import com.arbitrage.scanner.services.ArbitrageFinder
import com.arbitrage.scanner.services.ArbitrageFinderParallelImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            jsonModule,
            repositoryModule,
            businessLogicModule,
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

val repositoryModule = module {
    single<CexPriceRepository> { InMemoryCexPriceRepository() }
    single<ArbitrageOpportunityRepository> { InMemoryArbitrageOpportunityRepository() }
}

val businessLogicModule = module {
    factory<ArbitrageFinder> { ArbitrageFinderParallelImpl() }
}

val businessLogicProcessorModule = module {
    factory<BusinessLogicProcessorImplDeps> {
        object : BusinessLogicProcessorImplDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
            override val cexPriceRepository: CexPriceRepository = get()
            override val arbitrageOpportunityRepository: ArbitrageOpportunityRepository = get()
            override val arbitrageFinder: ArbitrageFinder = get()
        }
    }
    factory<BusinessLogicProcessor> { BusinessLogicProcessorImpl(get()) }
}

val loggingModule = module {
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}