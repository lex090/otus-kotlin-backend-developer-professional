package com.arbitrage.scanner

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.LockToken
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.random.Random

/**
 * Фабрика для создания тестовых данных, используемых в тестах
 *
 * Централизует логику создания тестовых объектов для обеспечения консистентности
 * и переиспользования кода в тестах
 */
object StubsDataFactory {

    /**
     * Создает тестовую цену CEX
     *
     * @param token Название токена (по умолчанию "BTC")
     * @param exchange Название биржи (по умолчанию "binance")
     * @param price Цена токена (по умолчанию 50000.0)
     * @param timestamp Временная метка (по умолчанию 1640995200)
     * @return Объект CexPrice
     */
    fun createCexPrice(
        token: String = "BTC",
        exchange: String = "binance",
        price: Double = 50000.0,
        timestamp: Long = 1640995200
    ): CexPrice = CexPrice(
        tokenId = CexTokenId(token),
        exchangeId = CexExchangeId(exchange),
        priceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(price)),
        timeStamp = Timestamp(timestamp)
    )

    /**
     * Создает тестовую арбитражную возможность
     *
     * @param id ID возможности (по умолчанию пустая строка, что означает DEFAULT)
     * @param token Название токена (по умолчанию "BTC")
     * @param buyExchange Биржа для покупки (по умолчанию "binance")
     * @param sellExchange Биржа для продажи (по умолчанию "okx")
     * @param buyPrice Цена покупки (по умолчанию 50000.0)
     * @param sellPrice Цена продажи (по умолчанию 51000.0)
     * @param spread Спред в процентах (по умолчанию 2.0)
     * @param startTimestamp Временная метка начала (по умолчанию 1640995200)
     * @param endTimestamp Временная метка окончания (по умолчанию null)
     * @param lockToken Токен для оптимистичной блокировки (по умолчанию пустая строка, что означает DEFAULT)
     * @return Объект CexToCexArbitrageOpportunity
     */
    fun createArbitrageOpportunity(
        id: String = "",
        token: String = "BTC",
        buyExchange: String = "binance",
        sellExchange: String = "okx",
        buyPrice: Double = 50000.0,
        sellPrice: Double = 51000.0,
        spread: Double = 2.0,
        startTimestamp: Long = 1640995200,
        endTimestamp: Long? = null,
        lockToken: String = ""
    ): CexToCexArbitrageOpportunity = CexToCexArbitrageOpportunity(
        id = if (id.isNotEmpty()) ArbitrageOpportunityId(id) else ArbitrageOpportunityId.NONE,
        cexTokenId = CexTokenId(token),
        buyCexExchangeId = CexExchangeId(buyExchange),
        sellCexExchangeId = CexExchangeId(sellExchange),
        buyCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(buyPrice)),
        sellCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(sellPrice)),
        spread = ArbitrageOpportunitySpread(spread),
        startTimestamp = Timestamp(startTimestamp),
        endTimestamp = endTimestamp?.let { Timestamp(it) },
        lockToken = if (lockToken.isNotEmpty()) LockToken(lockToken) else LockToken.NONE
    )

    /**
     * Создает список тестовых цен для заданного количества токенов и бирж
     *
     * @param tokenCount Количество токенов
     * @param exchangeCount Количество бирж
     * @param seed Seed для детерминированной генерации (по умолчанию 42)
     * @param priceVariation Вариация цен в процентах (по умолчанию 0.05 = 5%)
     * @return Список объектов CexPrice
     */
    fun generateCexPrices(
        tokenCount: Int,
        exchangeCount: Int,
        seed: Int = 42,
        priceVariation: Double = 0.05
    ): List<CexPrice> {
        val random = Random(seed)
        val prices = mutableListOf<CexPrice>()
        val exchanges = EXCHANGES

        for (i in 1..tokenCount) {
            val basePrice = random.nextDouble(10.0, 100000.0)
            for (j in 0 until exchangeCount.coerceAtMost(exchanges.size)) {
                val variation = if (priceVariation > 0.0) {
                    1.0 + random.nextDouble(-priceVariation, priceVariation)
                } else {
                    1.0
                }
                prices.add(
                    createCexPrice(
                        token = "TOKEN$i",
                        exchange = exchanges[j],
                        price = basePrice * variation
                    )
                )
            }
        }
        return prices
    }

    /**
     * Создает стандартный набор CEX цен для тестирования
     *
     * Возвращает предопределенный набор цен для основных токенов на разных биржах:
     * - BTC на Binance и OKX
     * - ETH на Binance и Bybit
     * - USDT на Binance
     *
     * @param timestamp Временная метка (по умолчанию 1640995200000)
     * @return Набор объектов CexPrice
     */
    fun getDefaultCexPrices(timestamp: Long = 1640995200000): Set<CexPrice> = setOf(
        createCexPrice(
            token = "BTC",
            exchange = "binance",
            price = 50000.0,
            timestamp = timestamp
        ),
        createCexPrice(
            token = "BTC",
            exchange = "okx",
            price = 51000.0,
            timestamp = timestamp
        ),
        createCexPrice(
            token = "ETH",
            exchange = "binance",
            price = 4000.0,
            timestamp = timestamp
        ),
        createCexPrice(
            token = "ETH",
            exchange = "bybit",
            price = 4050.0,
            timestamp = timestamp
        ),
        createCexPrice(
            token = "USDT",
            exchange = "binance",
            price = 1.0,
            timestamp = timestamp
        )
    )

    /**
     * Предопределенные биржи для тестов
     */
    val EXCHANGES = listOf(
        "binance", "okx", "bybit", "kraken", "coinbase",
        "huobi", "kucoin", "gate", "bitfinex", "gemini"
    )
}
