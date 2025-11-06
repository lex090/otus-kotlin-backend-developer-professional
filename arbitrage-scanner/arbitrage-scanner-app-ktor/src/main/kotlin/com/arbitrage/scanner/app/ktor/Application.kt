package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.app.ktor.koin.configureKoin
import com.arbitrage.scanner.app.ktor.routing.configureRouting
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureRouting()
}
