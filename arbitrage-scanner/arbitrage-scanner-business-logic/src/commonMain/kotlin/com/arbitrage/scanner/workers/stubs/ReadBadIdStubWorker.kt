package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.arbitrage.scanner.base.StubCase
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.readBadIdStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == StubCase.BAD_ID && state == State.RUNNING }
    handle {
        fail(
            InternalError(
                code = "bad-id",
                group = "stub",
                field = "id",
                message = "Некорректный идентификатор арбитражной возможности"
            )
        )
    }
}