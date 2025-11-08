package com.arbitrage.scanner.repository.postgres

import org.jetbrains.exposed.sql.Table

/**
 * Exposed DSL определение таблицы arbitrage_opportunities.
 *
 * **ВАЖНО**: Используется ТОЛЬКО для query DSL (SELECT, INSERT, UPDATE, DELETE).
 * НЕ используется для создания схемы! Схема управляется через Liquibase миграции.
 */
object ArbitrageOpportunitiesTable : Table("arbitrage_opportunities") {
    val id = varchar("id", 36)                              // UUID string
    val tokenId = varchar("token_id", 255).index()          // Indexed for filtering
    val buyExchangeId = varchar("buy_exchange_id", 255).index()
    val sellExchangeId = varchar("sell_exchange_id", 255).index()
    val buyPriceRaw = decimal("buy_price_raw", 30, 18)      // BigDecimal with 18 decimals
    val sellPriceRaw = decimal("sell_price_raw", 30, 18)
    val spread = double("spread").index()                    // Indexed for filtering
    val startTimestamp = long("start_timestamp")             // Unix epoch millis
    val endTimestamp = long("end_timestamp").nullable()      // Nullable
    val lockToken = varchar("lock_token", 36)                // UUID token for optimistic locking

    override val primaryKey = PrimaryKey(id, name = "PK_ArbitrageOpportunities")
}
