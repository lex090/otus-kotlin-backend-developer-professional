package com.arbitrage.scanner.app.ktor.koin

import com.arbitrage.scanner.app.ktor.koin.modules.blModule
import com.arbitrage.scanner.app.ktor.koin.modules.jsonModule
import com.arbitrage.scanner.app.ktor.koin.modules.loggingModule
import com.arbitrage.scanner.app.ktor.koin.modules.postgresModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            jsonModule,
            postgresModule,
            blModule,
            loggingModule,
        )
    }
}
