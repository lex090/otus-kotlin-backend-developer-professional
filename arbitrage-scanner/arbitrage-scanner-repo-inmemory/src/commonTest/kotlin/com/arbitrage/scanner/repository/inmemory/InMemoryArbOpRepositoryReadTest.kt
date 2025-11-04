package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.repository.ArbOpRepositoryInit
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpReadTest


/**
 * Тесты операций чтения для InMemoryArbOpRepository.
 */
class InMemoryArbOpRepositoryReadTest : RepositoryArbOpReadTest() {

    override fun createRepository(): IArbOpRepository = ArbOpRepositoryInit(
        repo = InMemoryArbOpRepository(),
        initItems = initObject,
    )
}
