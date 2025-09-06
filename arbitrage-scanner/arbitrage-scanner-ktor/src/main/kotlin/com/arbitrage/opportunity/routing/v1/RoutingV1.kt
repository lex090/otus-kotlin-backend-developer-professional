package com.arbitrage.opportunity.routing.v1

import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.routingV1() {
    route("/v1") {
        route("arbitrage_opportunities") {
            post("read") {
//                call.
            }
            post("update") {

            }
        }
    }
}