package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::getCexPricesWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.getCexPricesWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Этот обработчик получает все цены символов по всем доступным биржам
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            cexPrices.addAll(cexPriceClientService.getAllCexPrice().toList())
        }
    }
}
