package com.arbitrage.scanner.routing.v1

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.routingV1(
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
) {
    route("v1") {
        route("arbitrage_opportunities") {
            post("read") {
                call.readArbitrageOpportunity(
                    businessLogicProcessor = businessLogicProcessor,
                    loggerProvider = loggerProvider
                )
            }
            post("search") {
                call.searchArbitrageOpportunity(
                    businessLogicProcessor = businessLogicProcessor,
                    loggerProvider = loggerProvider
                )
            }
        }
    }
}
