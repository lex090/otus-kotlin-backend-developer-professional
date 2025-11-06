package com.arbitrage.scanner.app.ktor.koin

import com.arbitrage.scanner.app.ktor.koin.modules.jsonModule
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoinTest(module: Module) {
    install(Koin) {
        modules(
            jsonModule,
            module,
            module {
                factory<ArbScanLoggerProvider> { ArbScanLoggerProvider() }
            }
        )
    }
}
