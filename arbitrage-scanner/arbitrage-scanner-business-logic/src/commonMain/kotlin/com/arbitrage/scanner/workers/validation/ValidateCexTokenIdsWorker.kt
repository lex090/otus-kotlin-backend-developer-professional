package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки ID CEX токенов в фильтре поиска.
 * Проверяет корректность форматов для CexTokenId.
 *
 * Правила валидации:
 * - ID не должен быть пустым (если указан)
 * - Минимальная длина: 2 символа (например, "BTC", "ETH")
 * - Максимальная длина: 50 символов
 * - Допустимые символы: буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateCexTokenIdsWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        filter.cexTokenIdsFilter.isNotNone() && filter.cexTokenIdsFilter.value.any { !isValidTokenId(it.value) }
    }
    handle {
        val filter = arbitrageOpportunitySearchRequestValidating
        val invalidCexTokens = filter.cexTokenIdsFilter.value.filter { !isValidTokenId(it.value) }

        fail(
            InternalError(
                code = "validation-format",
                group = "validation",
                field = "cexTokenIds",
                message = "Некорректный формат ID CEX токенов: ${invalidCexTokens.joinToString(", ") { it.value }}. " +
                        "ID токена должен содержать от $MIN_TOKEN_ID_LENGTH до $MAX_TOKEN_ID_LENGTH символов и состоять только из букв, цифр и дефисов. " +
                        "Примеры корректных ID: BTC, ETH, USDT, BNB-USDT"
            )
        )
    }
}
