package com.arbitrage.scanner.workers.search

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::prepareSearchResponseWorker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.prepareSearchResponseWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Подготовка ответа для операции search.
        Устанавливает финальное состояние обработки.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            logger.info("Подготовка ответа для search завершена. Найдено: ${arbitrageOpportunitySearchResponse.size} возможностей")
            state = State.FINISHING
        }
    }
}
