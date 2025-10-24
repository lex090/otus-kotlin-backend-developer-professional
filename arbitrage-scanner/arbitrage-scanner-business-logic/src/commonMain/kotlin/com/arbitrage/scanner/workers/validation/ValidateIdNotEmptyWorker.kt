package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки ID на пустоту.
 * Проверяет, что ID не является пустой строкой и не состоит только из пробелов.
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateIdNotEmptyWorker(
    title: String,
) = worker {
    this.title = title
    on { arbitrageOpportunityReadRequestValidating.value.isBlank() }
    handle {
        fail(
            InternalError(
                code = "validation-empty",
                group = "validation",
                field = "id",
                message = "ID арбитражной возможности не должен быть пустым"
            )
        )
    }
}
