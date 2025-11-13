package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки диапазона спредов (minSpread и maxSpread).
 *
 * Правила валидации:
 * - minSpread не может быть отрицательным
 * - maxSpread не может быть отрицательным (если указан)
 * - maxSpread не может быть больше 100 (если указан)
 * - Если maxSpread указан и minSpread не DEFAULT, то minSpread должен быть <= maxSpread
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateSpreadRangeWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        val minSpread = filter.minSpread.value
        val maxSpread = filter.maxSpread?.value

        // Проверяем на отрицательные значения
        val hasNegativeMin = minSpread < 0
        val hasNegativeMax = maxSpread != null && maxSpread < 0
        val hasMaxOver100 = maxSpread != null && maxSpread > 100

        // Проверяем корректность диапазона
        val hasInvalidRange = maxSpread != null && minSpread > maxSpread

        hasNegativeMin || hasNegativeMax || hasMaxOver100 || hasInvalidRange
    }
    handle {
        val filter = arbitrageOpportunitySearchRequestValidating
        val minSpread = filter.minSpread.value
        val maxSpread = filter.maxSpread?.value

        when {
            minSpread < 0 -> {
                fail(
                    InternalError(
                        code = "validation-range",
                        group = "validation",
                        field = "minSpread",
                        message = "minSpread не может быть отрицательным. Текущее значение: $minSpread"
                    )
                )
            }
            maxSpread != null && maxSpread < 0 -> {
                fail(
                    InternalError(
                        code = "validation-range",
                        group = "validation",
                        field = "maxSpread",
                        message = "maxSpread не может быть отрицательным. Текущее значение: $maxSpread"
                    )
                )
            }
            maxSpread != null && maxSpread > 100 -> {
                fail(
                    InternalError(
                        code = "validation-range",
                        group = "validation",
                        field = "maxSpread",
                        message = "maxSpread не может быть больше 100. Текущее значение: $maxSpread"
                    )
                )
            }
            maxSpread != null && minSpread > maxSpread -> {
                fail(
                    InternalError(
                        code = "validation-range",
                        group = "validation",
                        field = "spread",
                        message = "minSpread ($minSpread) не может быть больше maxSpread ($maxSpread)"
                    )
                )
            }
        }
    }
}
