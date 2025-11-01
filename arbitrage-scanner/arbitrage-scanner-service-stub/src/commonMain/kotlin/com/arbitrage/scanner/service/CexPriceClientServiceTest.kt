package com.arbitrage.scanner.service

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.CexPrice

/**
 * Test реализация сервиса для получения цен с CEX бирж.
 * Используется для тестирования и разработки без реального подключения к биржам.
 *
 * Использует [StubsDataFactory] для получения предопределенного набора тестовых данных.
 */
class CexPriceClientServiceTest : CexPriceClientService {

    override suspend fun getAllCexPrice(): Set<CexPrice> {
        return StubsDataFactory.getDefaultCexPrices()
    }
}
