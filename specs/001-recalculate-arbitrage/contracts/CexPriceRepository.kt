package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexExchangeId

/**
 * Репозиторий для управления ценовыми данными с CEX бирж
 *
 * Этот интерфейс определяет контракт для доступа к ценовым данным криптовалют
 * с централизованных бирж (CEX - Centralized Exchange).
 *
 * Реализации могут быть:
 * - In-memory (для тестирования и MVP)
 * - Database-backed (PostgreSQL, MongoDB)
 * - Cached (Redis + Database)
 */
interface CexPriceRepository {
    /**
     * Получить все доступные цены
     *
     * @return список всех ценовых записей
     */
    suspend fun findAll(): List<CexPrice>

    /**
     * Найти цены по токену
     *
     * Возвращает все цены указанного токена на разных биржах.
     * Полезно для анализа ценовых расхождений конкретного токена.
     *
     * @param tokenId идентификатор токена (например, "BTC", "ETH")
     * @return список цен для указанного токена на разных биржах
     */
    suspend fun findByToken(tokenId: CexTokenId): List<CexPrice>

    /**
     * Найти цены по бирже
     *
     * Возвращает цены всех токенов на указанной бирже.
     * Полезно для анализа ценовой ситуации на конкретной бирже.
     *
     * @param exchangeId идентификатор биржи (например, "BINANCE", "OKX")
     * @return список цен различных токенов на указанной бирже
     */
    suspend fun findByExchange(exchangeId: CexExchangeId): List<CexPrice>

    /**
     * Сохранить одну ценовую запись
     *
     * Если запись с таким же токеном и биржей уже существует,
     * она должна быть заменена новой (upsert семантика).
     *
     * @param price ценовая запись для сохранения
     */
    suspend fun save(price: CexPrice)

    /**
     * Сохранить множество ценовых записей
     *
     * Batch операция для эффективного сохранения большого количества цен.
     * Должна быть атомарной (все или ничего) где это возможно.
     *
     * @param prices список ценовых записей
     */
    suspend fun saveAll(prices: List<CexPrice>)

    /**
     * Очистить все ценовые данные
     *
     * ВНИМАНИЕ: Деструктивная операция! Удаляет все данные из репозитория.
     * Используется в тестах и для сброса состояния.
     */
    suspend fun clear()
}
