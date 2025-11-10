package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpSearchTest


/**
 * Тесты операций поиска для InMemoryArbOpRepository.
 */
class InMemoryArbOpRepositorySearchTest : RepositoryArbOpSearchTest() {

    override fun createRepository(): IArbOpRepository = ArbOpRepositoryInit(
        repo = InMemoryArbOpRepository(),
        initItems = initObject,
    )
}
