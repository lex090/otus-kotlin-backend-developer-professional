package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки ID CEX бирж в фильтре поиска.
 * Проверяет корректность форматов для CexExchangeId.
 *
 * Правила валидации:
 * - ID не должен быть пустым (если указан)
 * - Минимальная длина: 3 символа (например, "OKX")
 * - Максимальная длина: 50 символов
 * - Допустимые символы: буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 *
 * Примеры корректных ID:
 * - binance, okx, bybit, kraken, coinbase
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateCexExchangeIdsWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        filter.cexExchangeIds.any { !isValidExchangeId(it.value) }
    }
    handle {
        val filter = arbitrageOpportunitySearchRequestValidating
        val invalidCexExchanges = filter.cexExchangeIds.filter { !isValidExchangeId(it.value) }

        fail(
            InternalError(
                code = "validation-format",
                group = "validation",
                field = "cexExchangeIds",
                message = "Некорректный формат ID CEX бирж: ${invalidCexExchanges.joinToString(", ") { it.value }}. " +
                        "ID биржи должен содержать от 3 до 50 символов и состоять только из букв, цифр и дефисов. " +
                        "Примеры корректных ID: binance, okx, bybit, kraken, coinbase"
            )
        )
    }
}
