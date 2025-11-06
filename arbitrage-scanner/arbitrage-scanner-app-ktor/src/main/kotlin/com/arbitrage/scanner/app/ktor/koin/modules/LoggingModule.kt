package com.arbitrage.scanner.app.ktor.koin.modules

import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import org.koin.dsl.module

val loggingModule = module {
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}
