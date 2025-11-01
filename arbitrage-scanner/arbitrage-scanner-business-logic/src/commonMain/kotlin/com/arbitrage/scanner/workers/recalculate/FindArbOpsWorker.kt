package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction
import kotlin.time.measureTimedValue

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::findArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.findArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Этот обработчик ищет все возможные арбитражные возможности.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val result = measureTimedValue {
                config.cexToCexArbitrageFinder.findOpportunities(cexPrices)
            }
            arbOps.addAll(result.value)
            executionTimeOfFindArbOps = result.duration
        }
    }
}