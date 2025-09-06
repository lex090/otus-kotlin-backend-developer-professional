package com.arbitrage.opportunity.routing.v1

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

fun Application.routingV1() {
    val json: Json by inject<Json>()
    routing {
        install(ContentNegotiation) {
            json(json = json)
        }
        route("/v1") {
            route("arbitrage_opportunities") {
                post("read") {

                }
                post("update") {

                }
            }
        }
    }
}