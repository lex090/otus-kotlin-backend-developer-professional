package com.arbitrage.opportunity

import com.arbitrage.opportunity.routing.configureRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureRouting()
}
