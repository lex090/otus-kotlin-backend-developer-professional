package com.arbitrage.scanner.workers

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.chain

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.commandProcessor(
    title: String,
    command: Command,
    block: ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.() -> Unit,
) = chain {
    this.title = title
    on { this.command == command && state == State.RUNNING }
    block()
}
