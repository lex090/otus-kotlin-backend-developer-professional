package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.app.ktor.koin.configureKoin
import com.arbitrage.scanner.app.ktor.routing.configureRouting
import com.arbitrage.scanner.repository.postgres.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureRouting()

    // Закрываем connection pool при shutdown приложения
    monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}
