package com.arbitrage.scanner.workers

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.chain

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validationProcessor(
    block: ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.() -> Unit,
) = chain {
    this.title = "Процесс валидации входящих данных"
    on { state == State.RUNNING }
    block()
}
