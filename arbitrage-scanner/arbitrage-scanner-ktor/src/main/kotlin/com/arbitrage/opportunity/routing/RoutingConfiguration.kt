package com.arbitrage.opportunity.routing

import com.arbitrage.opportunity.routing.v1.routingV1
import io.ktor.server.application.Application

fun Application.configureRouting() {
    routingV1()
}