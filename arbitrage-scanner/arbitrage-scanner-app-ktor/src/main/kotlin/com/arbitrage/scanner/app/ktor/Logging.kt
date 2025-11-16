package com.arbitrage.scanner.app.ktor

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO

        format { call ->
            val status = call.response.status()?.value ?: 0
            val method = call.request.httpMethod.value
            val uri = call.request.uri
            val duration = call.processingTimeMillis()

            // Структурированный формат для парсинга в OpenSearch
            "http_method=$method http_status=$status response_time_ms=$duration uri=$uri"
        }
    }
}
