package com.arbitrage.scanner

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
    factory<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
}