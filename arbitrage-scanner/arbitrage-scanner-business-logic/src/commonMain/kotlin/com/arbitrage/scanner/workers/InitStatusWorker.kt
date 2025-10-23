package com.arbitrage.scanner.workers

import com.arbitrage.scanner.BusinessLogicProcessorStubsDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorStubsDeps>.initStatus(
    title: String
) = worker {
    this.title = title
    this.description = """
        Этот обработчик устанавливает стартовый статус обработки. Запускается только в случае не заданного статуса.
    """.trimIndent()
    on { state == State.NONE }
    handle { state = State.RUNNING }
}
