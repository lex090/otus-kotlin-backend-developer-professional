package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки минимального значения спреда в фильтре поиска.
 * Проверяет, что спред не является отрицательным.
 *
 * Правило валидации:
 * - Спред не может быть отрицательным (должен быть >= 0.0)
 * - Значение 0.0 допустимо (означает "любой спред")
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateSpreadMinWorker(
    title: String,
) = worker {
    this.title = title
    on { arbitrageOpportunitySearchRequestValidating.spread.value < MIN_SPREAD_VALUE }
    handle {
        val spread = arbitrageOpportunitySearchRequestValidating.spread.value
        fail(
            InternalError(
                code = "validation-range",
                group = "validation",
                field = "spread",
                message = "Минимальный спред не может быть отрицательным. " +
                        "Текущее значение: $spread. " +
                        "Минимально допустимое значение: $MIN_SPREAD_VALUE"
            )
        )
    }
}

private const val MIN_SPREAD_VALUE = 0.0
