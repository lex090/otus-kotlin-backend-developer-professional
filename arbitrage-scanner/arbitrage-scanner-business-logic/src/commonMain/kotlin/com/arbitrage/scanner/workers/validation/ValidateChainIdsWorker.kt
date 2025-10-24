package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.arbitrage.scanner.models.DexChainId
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки ID блокчейнов в фильтре поиска.
 * Проверяет корректность форматов для DexChainId.
 *
 * Правила валидации:
 * - ID не должен быть пустым (если указан)
 * - Минимальная длина: 3 символа (например, "BSC", "ETH")
 * - Максимальная длина: 50 символов
 * - Допустимые символы: буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 *
 * Примеры корректных ID:
 * - bsc, ethereum, polygon, avalanche, arbitrum, optimism
 * - eth-mainnet, bsc-testnet
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateChainIdsWorker(
    title: String,
) = worker {
    this.title = title
    on {
        val filter = arbitrageOpportunitySearchRequestValidating
        filter.dexChainIds.any { !isValidChainId(it.value) }
    }
    handle {
        val filter = arbitrageOpportunitySearchRequestValidating
        val invalidChains = filter.dexChainIds.filter { !isValidChainId(it.value) }

        fail(
            InternalError(
                code = "validation-format",
                group = "validation",
                field = "chainIds",
                message = "Некорректный формат ID блокчейнов: ${invalidChains.joinToString(", ") { it.value }}. " +
                        "ID блокчейна должен содержать от 3 до 50 символов и состоять только из букв, цифр и дефисов. " +
                        "Примеры корректных ID: bsc, ethereum, polygon, eth-mainnet"
            )
        )
    }
}

/**
 * Проверка валидности ID блокчейна
 */
private fun isValidChainId(id: String): Boolean {
    // Пустая строка допустима для пустых фильтров
    if (id.isEmpty()) return true

    // Проверка длины
    if (id.length !in MIN_CHAIN_ID_LENGTH..MAX_CHAIN_ID_LENGTH) return false

    // Проверка формата: буквы, цифры и дефисы
    val chainIdRegex = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$")
    val shortChainRegex = Regex("^[a-zA-Z0-9]{3}$")

    return when (id.length) {
        3 -> id.matches(shortChainRegex)
        else -> id.matches(chainIdRegex)
    }
}

private const val MIN_CHAIN_ID_LENGTH = 3
private const val MAX_CHAIN_ID_LENGTH = 50