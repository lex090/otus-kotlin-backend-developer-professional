package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки максимального значения спреда в фильтре поиска.
 * Проверяет, что спред не превышает максимально допустимое значение.
 *
 * Правило валидации:
 * - Максимальное значение: 100% (разумный предел для процентного спреда)
 *
 * Примечание: спред обычно представляет собой процентную разницу между ценами
 * на разных биржах, поэтому значения выше 100% крайне маловероятны в реальной торговле.
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateSpreadMaxWorker(
    title: String,
) = worker {
    this.title = title
    on { arbitrageOpportunitySearchRequestValidating.spread.value > MAX_SPREAD_VALUE }
    handle {
        val spread = arbitrageOpportunitySearchRequestValidating.spread.value
        fail(
            InternalError(
                code = "validation-range",
                group = "validation",
                field = "spread",
                message = "Минимальный спред слишком большой. " +
                        "Текущее значение: $spread%. " +
                        "Максимально допустимое значение: $MAX_SPREAD_VALUE%"
            )
        )
    }
}

private const val MAX_SPREAD_VALUE = 100.0
