package com.arbitrage.scanner.repository

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.errorSystem
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import kotlin.coroutines.cancellation.CancellationException

/**
 * Базовый класс исключений репозитория.
 * Содержит InternalError для передачи структурированной информации об ошибке.
 */
open class RepositoryException(val error: InternalError) : RuntimeException(error.message)

suspend fun IArbOpRepository.tryExecute(block: suspend () -> ArbOpRepoResponse): ArbOpRepoResponse =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: RepositoryException) {
        // Для RepositoryException возвращаем вложенную ошибку
        ArbOpRepoResponse.Error(listOf(e.error))
    } catch (e: Throwable) {
        ArbOpRepoResponse.Error(listOf(errorSystem("repo-exception", e)))
    }