package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки формата ID.
 * Проверяет, что ID соответствует допустимому формату:
 * - Содержит только буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 * - Начинается и заканчивается буквой или цифрой (не дефисом)
 *
 * Примеры корректных ID:
 * - "abc123"
 * - "test-id-123"
 * - "1a2b3c"
 *
 * Примеры некорректных ID:
 * - "-test" (начинается с дефиса)
 * - "test-" (заканчивается дефисом)
 * - "test_id" (содержит подчеркивание)
 * - "test@id" (содержит спецсимвол)
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateIdFormatWorker(
    title: String,
) = worker {
    this.title = title
    on { !isValidFormat(arbitrageOpportunityReadRequestValidating.value) }
    handle {
        fail(
            InternalError(
                code = "validation-format",
                group = "validation",
                field = "id",
                message = "ID арбитражной возможности содержит недопустимые символы. " +
                        "Разрешены только буквы (a-z, A-Z), цифры (0-9) и дефисы (-). " +
                        "ID должен начинаться и заканчиваться буквой или цифрой"
            )
        )
    }
}

private fun isValidFormat(id: String): Boolean {
    val idFormatRegex = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$")
    return id.matches(idFormatRegex)
}