package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpUpdateTest


/**
 * Тесты операций обновления для InMemoryArbOpRepository.
 */
class InMemoryArbOpRepositoryUpdateTest : RepositoryArbOpUpdateTest() {

    override fun createRepository(): IArbOpRepository = ArbOpRepositoryInit(
        repo = InMemoryArbOpRepository(),
        initItems = initObject,
    )
}
