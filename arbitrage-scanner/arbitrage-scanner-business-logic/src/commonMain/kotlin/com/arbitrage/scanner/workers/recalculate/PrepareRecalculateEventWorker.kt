package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.RecalculateResult
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::prepareRecalculateResponseWorker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.prepareRecalculateResponseWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Подготовка конечного ответа.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            recalculateResponse = RecalculateResult(
                opportunitiesCount = arbOps.size,
                processingTimeMs = executionTimeOfFindArbOps.inWholeMilliseconds
            )
            state = State.FINISHING
        }
    }
}
