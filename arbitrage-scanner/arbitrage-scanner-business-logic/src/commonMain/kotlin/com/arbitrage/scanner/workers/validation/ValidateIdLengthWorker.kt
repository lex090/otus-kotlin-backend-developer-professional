package com.arbitrage.scanner.workers.validation

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

/**
 * Валидатор проверки минимальной длины ID.
 * Проверяет, что длина ID не меньше минимально допустимой.
 *
 * Минимальная длина: 3 символа
 * Минимум 3 символа необходим для обеспечения уникальности и читаемости
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateIdMinLengthWorker(
    title: String,
) = worker {
    this.title = title
    on { arbitrageOpportunityReadRequestValidating.value.length < MIN_ID_LENGTH }
    handle {
        val id = arbitrageOpportunityReadRequestValidating.value
        fail(
            InternalError(
                code = "validation-length",
                group = "validation",
                field = "id",
                message = "ID арбитражной возможности слишком короткий. " +
                        "Минимальная длина: $MIN_ID_LENGTH символа, " +
                        "текущая длина: ${id.length}"
            )
        )
    }
}

/**
 * Валидатор проверки максимальной длины ID.
 * Проверяет, что длина ID не превышает максимально допустимую.
 *
 * Максимальная длина: 50 символов
 * Максимум 50 символов - разумный предел для хранения и отображения
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.validateIdMaxLengthWorker(
    title: String,
) = worker {
    this.title = title
    on { arbitrageOpportunityReadRequestValidating.value.length > MAX_ID_LENGTH }
    handle {
        val id = arbitrageOpportunityReadRequestValidating.value
        fail(
            InternalError(
                code = "validation-length",
                group = "validation",
                field = "id",
                message = "ID арбитражной возможности слишком длинный. " +
                        "Максимальная длина: $MAX_ID_LENGTH символов, " +
                        "текущая длина: ${id.length}"
            )
        )
    }
}

private const val MIN_ID_LENGTH = 3
private const val MAX_ID_LENGTH = 50