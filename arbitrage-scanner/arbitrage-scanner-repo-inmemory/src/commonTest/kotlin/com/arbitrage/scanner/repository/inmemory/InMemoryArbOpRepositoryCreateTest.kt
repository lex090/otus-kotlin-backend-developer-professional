package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.RepositoryArbOpCreateTest

/**
 * Тесты операций создания для InMemoryArbOpRepository.
 */
class InMemoryArbOpRepositoryCreateTest : RepositoryArbOpCreateTest() {

    override fun createRepository(): IArbOpRepository = InMemoryArbOpRepository()
}
