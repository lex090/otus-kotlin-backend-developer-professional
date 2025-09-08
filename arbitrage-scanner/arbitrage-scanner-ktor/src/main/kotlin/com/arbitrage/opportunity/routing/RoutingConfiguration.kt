package com.arbitrage.opportunity.routing

import com.arbitrage.opportunity.BusinessLogicProcessor
import com.arbitrage.opportunity.routing.v1.routingV1
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val json: Json by inject<Json>()
    val businessLogicProcessor: BusinessLogicProcessor by inject<BusinessLogicProcessor>()

    routing {
        install(ContentNegotiation) {
            json(json = json)
        }
        routingV1(businessLogicProcessor = businessLogicProcessor)
    }
}