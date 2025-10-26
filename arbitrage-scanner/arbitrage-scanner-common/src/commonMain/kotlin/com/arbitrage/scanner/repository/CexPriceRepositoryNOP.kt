package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId

class CexPriceRepositoryNOP : CexPriceRepository {
    override suspend fun findAll(): List<CexPrice> = throw NotImplementedError("Not used in READ tests")
    override suspend fun findByToken(tokenId: CexTokenId): List<CexPrice> =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun findByExchange(exchangeId: CexExchangeId): List<CexPrice> =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun save(price: CexPrice) = throw NotImplementedError("Not used in READ tests")
    override suspend fun saveAll(prices: List<CexPrice>) = throw NotImplementedError("Not used in READ tests")
    override suspend fun clear() = throw NotImplementedError("Not used in READ tests")
}
