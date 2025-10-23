package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.readNotFoundStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == Stubs.NOT_FOUND && state == State.RUNNING }
    handle {
        fail(
            InternalError(
                code = "not-found",
                group = "stub",
                field = "id",
                message = "Арбитражная возможность не найдена"
            )
        )
    }
}