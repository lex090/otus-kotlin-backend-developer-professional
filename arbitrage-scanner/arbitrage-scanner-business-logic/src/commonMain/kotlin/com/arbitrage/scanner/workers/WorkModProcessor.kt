package com.arbitrage.scanner.workers

import com.arbitrage.scanner.BusinessLogicProcessorStubsDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.chain

fun ICorAddExecDsl<Context, BusinessLogicProcessorStubsDeps>.workModProcessor(
    title: String,
    workMode: WorkMode,
    block: ICorAddExecDsl<Context, BusinessLogicProcessorStubsDeps>.() -> Unit,
) = chain {
    this.title = title
    on { this.workMode == workMode && state == State.RUNNING }
    block()
}
