package com.arbitrage.opportunity

import com.arbitrage.scanner.ArbOpStubs
import com.arbitrage.scanner.base.State
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger(level = Level.INFO)
        modules(
            jsonModule,
            logicProcessor,
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

val logicProcessor = module {
    factory<LogicProcessor> {
        LogicProcessor { context ->
            context.state = State.RUNNING
            context.arbitrageOpportunityReadResponse = ArbOpStubs.arbitrageOpportunity
            context.arbitrageOpportunitySearchResponse.add(ArbOpStubs.arbitrageOpportunity)
            context.state = State.FINISHING
        }
    }
}