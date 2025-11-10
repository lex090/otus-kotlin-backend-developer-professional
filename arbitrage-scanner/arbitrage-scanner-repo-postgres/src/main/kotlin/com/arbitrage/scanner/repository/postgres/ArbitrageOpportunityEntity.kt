package com.arbitrage.scanner.repository.postgres

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.LockToken
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * DTO модель для хранения арбитражной возможности в PostgreSQL репозитории.
 * Использует примитивные типы для эффективного хранения и сериализации.
 *
 * @property id Идентификатор возможности
 * @property tokenId Идентификатор токена
 * @property buyExchangeId Идентификатор биржи для покупки
 * @property sellExchangeId Идентификатор биржи для продажи
 * @property buyPriceRaw Сырая цена покупки (строковое представление BigDecimal)
 * @property sellPriceRaw Сырая цена продажи (строковое представление BigDecimal)
 * @property spread Спред в процентах
 * @property startTimestamp Временная метка начала возможности
 * @property endTimestamp Временная метка окончания возможности (nullable)
 * @property lockToken UUID токен для optimistic locking
 */
internal data class ArbitrageOpportunityEntity(
    val id: String,
    val tokenId: String,
    val buyExchangeId: String,
    val sellExchangeId: String,
    val buyPriceRaw: String,
    val sellPriceRaw: String,
    val spread: Double,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val lockToken: String
)

/**
 * Преобразует доменную модель в DTO для хранения в PostgreSQL.
 * lockToken берется из доменной модели.
 */
internal fun CexToCexArbitrageOpportunity.toEntity(): ArbitrageOpportunityEntity {
    return ArbitrageOpportunityEntity(
        id = id.value,
        tokenId = cexTokenId.value,
        buyExchangeId = buyCexExchangeId.value,
        sellExchangeId = sellCexExchangeId.value,
        buyPriceRaw = buyCexPriceRaw.value.toString(),
        sellPriceRaw = sellCexPriceRaw.value.toString(),
        spread = spread.value,
        startTimestamp = startTimestamp.value,
        endTimestamp = endTimestamp?.value,
        lockToken = lockToken.value
    )
}

/**
 * Преобразует DTO в доменную модель
 */
internal fun ArbitrageOpportunityEntity.toDomain(): CexToCexArbitrageOpportunity {
    return CexToCexArbitrageOpportunity(
        id = ArbitrageOpportunityId(id),
        cexTokenId = CexTokenId(tokenId),
        buyCexExchangeId = CexExchangeId(buyExchangeId),
        sellCexExchangeId = CexExchangeId(sellExchangeId),
        buyCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.parseString(buyPriceRaw)),
        sellCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.parseString(sellPriceRaw)),
        spread = ArbitrageOpportunitySpread(spread),
        startTimestamp = Timestamp(startTimestamp),
        endTimestamp = endTimestamp?.let { Timestamp(it) },
        lockToken = LockToken(lockToken)
    )
}
