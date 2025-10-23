package com.arbitrage.scanner

import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
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
    factory<BusinessLogicProcessorSimpleDeps> {
        object : BusinessLogicProcessorSimpleDeps {
            override val loggerProvider: ArbScanLoggerProvider = get()
        }
    }
    factory<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl(get()) }
}

val loggingModule = module {
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}