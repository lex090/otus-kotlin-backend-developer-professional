package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.ArbOpStubs
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.base.Stubs
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::recalculateSuccessStubWorker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.recalculateSuccessStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == Stubs.SUCCESS && state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.DEBUG) {
            recalculateResponse = ArbOpStubs.recalculateResult
            state = State.FINISHING
        }
    }
}
