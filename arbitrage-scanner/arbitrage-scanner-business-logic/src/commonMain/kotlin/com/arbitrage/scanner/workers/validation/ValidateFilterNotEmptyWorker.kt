package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки, что фильтр для поиска не пустой.
 * Проверяет, что хотя бы один критерий поиска задан.
 *
 * Пустой фильтр может привести к возврату слишком большого количества данных,
 * поэтому требуем указать хотя бы один критерий.
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateFilterNotEmptyWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        filter.cexTokenIds.isEmpty() &&
        filter.cexExchangeIds.isEmpty() &&
        filter.spread.value == 0.0
    }
    handle {
        fail(
            InternalError(
                code = "validation-empty",
                group = "validation",
                field = "filter",
                message = "Фильтр поиска не должен быть пустым. " +
                        "Укажите хотя бы один критерий поиска: токены, биржи или минимальный спред"
            )
        )
    }
}