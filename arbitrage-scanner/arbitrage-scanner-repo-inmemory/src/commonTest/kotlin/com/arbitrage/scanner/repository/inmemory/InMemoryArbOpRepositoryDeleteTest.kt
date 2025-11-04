package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpDeleteTest


/**
 * Тесты операций удаления для InMemoryArbOpRepository.
 */
class InMemoryArbOpRepositoryDeleteTest : RepositoryArbOpDeleteTest() {

    override fun createRepository(): IArbOpRepository = ArbOpRepositoryInit(
        repo = InMemoryArbOpRepository(),
        initItems = initObject,
    )
}
