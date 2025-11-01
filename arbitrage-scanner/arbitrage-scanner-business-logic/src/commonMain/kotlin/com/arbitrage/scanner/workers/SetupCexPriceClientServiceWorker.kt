package com.arbitrage.scanner.workers

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::setupCexPriceClientServiceWorker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.setupCexPriceClientServiceWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Установка рабочего сервиса CexPriceClientService в зависимости от запрошенного режима работы 
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            cexPriceClientService = when (workMode) {
                WorkMode.PROD -> config.prodCexPriceClientService
                WorkMode.TEST -> config.testCexPriceClientService
                WorkMode.STUB -> config.stubCexPriceClientService
            }
        }
    }
}