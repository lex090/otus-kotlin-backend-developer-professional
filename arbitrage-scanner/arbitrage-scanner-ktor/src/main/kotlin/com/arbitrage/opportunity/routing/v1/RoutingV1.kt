package com.arbitrage.opportunity.routing.v1

import com.arbitrage.opportunity.LogicProcessor
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.routingV1(logicProcessor: LogicProcessor) {
    route("v1") {
        route("arbitrage_opportunities") {
            post("read") {
                call.readArbitrageOpportunity(logicProcessor)
            }
            post("update") {
                call.searchArbitrageOpportunity(logicProcessor)
            }
        }
    }
}
