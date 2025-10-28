package com.arbitrage.scanner.repository

import com.arbitrage.scanner.errorSystem
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import kotlin.coroutines.cancellation.CancellationException

suspend fun IArbOpRepository.tryExecute(block: suspend () -> ArbOpRepoResponse): ArbOpRepoResponse =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        ArbOpRepoResponse.Error(listOf(errorSystem("repo-exception", e)))
    }