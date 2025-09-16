package com.arbitrage.scanner.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.mappers.toTransport
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlin.reflect.KFunction

suspend inline fun <reified Req : IRequest, reified Resp : IResponse> ApplicationCall.processRequest(
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
    kFun: KFunction<*>,
    logId: String,
) {
    processContext(
        prepareContextFromRequest = { fromTransport(request = receive<Req>()) },
        resolveContextToResponse = { respond(toTransport() as Resp) },
        businessLogicProcessor = businessLogicProcessor,
        loggerProvider = loggerProvider,
        kFun = kFun,
        logId = logId,
    )
}
