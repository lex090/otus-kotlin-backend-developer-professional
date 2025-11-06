package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.app.ktor.koin.configureKoinTest
import com.arbitrage.scanner.app.ktor.routing.configureRouting
import io.ktor.server.application.Application
import org.koin.core.module.Module

fun Application.moduleTest(module: Module) {
    configureKoinTest(module)
    configureRouting()
}
