package com.arbitrage.opportunity

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configureContextNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}