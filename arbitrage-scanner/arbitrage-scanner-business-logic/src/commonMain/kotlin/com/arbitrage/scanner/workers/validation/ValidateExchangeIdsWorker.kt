package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки ID бирж в фильтре поиска (buyExchangeIds и sellExchangeIds).
 * Проверяет корректность форматов для обоих множеств.
 *
 * Правила валидации:
 * - ID не должен быть пустым (если указан)
 * - Минимальная длина: 3 символа (например, "okx")
 * - Максимальная длина: 50 символов
 * - Допустимые символы: буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateExchangeIdsWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        val invalidBuyIds = filter.buyExchangeIds.value.filter { !isValidExchangeId(it.value) }
        val invalidSellIds = filter.sellExchangeIds.value.filter { !isValidExchangeId(it.value) }
        invalidBuyIds.isNotEmpty() || invalidSellIds.isNotEmpty()
    }
    handle {
        val filter = arbitrageOpportunitySearchRequestValidating
        val invalidBuyIds = filter.buyExchangeIds.value.filter { !isValidExchangeId(it.value) }
        val invalidSellIds = filter.sellExchangeIds.value.filter { !isValidExchangeId(it.value) }

        if (invalidBuyIds.isNotEmpty()) {
            fail(
                InternalError(
                    code = "validation-format",
                    group = "validation",
                    field = "buyExchangeIds",
                    message = "Некорректный формат ID бирж покупки: ${invalidBuyIds.joinToString(", ") { it.value }}. " +
                            "ID биржи должен содержать от 3 до 50 символов и состоять только из букв, цифр и дефисов. " +
                            "Примеры корректных ID: binance, okx, bybit, 1inch"
                )
            )
        }

        if (invalidSellIds.isNotEmpty()) {
            fail(
                InternalError(
                    code = "validation-format",
                    group = "validation",
                    field = "sellExchangeIds",
                    message = "Некорректный формат ID бирж продажи: ${invalidSellIds.joinToString(", ") { it.value }}. " +
                            "ID биржи должен содержать от 3 до 50 символов и состоять только из букв, цифр и дефисов. " +
                            "Примеры корректных ID: binance, okx, bybit, 1inch"
                )
            )
        }
    }
}
