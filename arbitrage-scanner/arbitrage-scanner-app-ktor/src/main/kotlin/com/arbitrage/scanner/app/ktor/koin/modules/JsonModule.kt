package com.arbitrage.scanner.app.ktor.koin.modules

import kotlinx.serialization.json.Json
import org.koin.dsl.module

val jsonModule = module {
    factory<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}
