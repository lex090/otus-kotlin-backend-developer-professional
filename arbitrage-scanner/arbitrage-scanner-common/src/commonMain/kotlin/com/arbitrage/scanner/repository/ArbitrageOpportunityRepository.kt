package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.base.Timestamp

/**
 * Репозиторий для управления арбитражными возможностями
 *
 * Этот интерфейс определяет контракт для сохранения и доступа к найденным
 * арбитражным возможностям между CEX биржами.
 *
 * Жизненный цикл возможности:
 * 1. Created (save) - возможность найдена и сохранена с startTimestamp
 * 2. Active (endTimestamp == null) - возможность актуальна
 * 3. Ended (markAsEnded) - возможность завершена с установленным endTimestamp
 */
interface ArbitrageOpportunityRepository {
    /**
     * Сохранить одну возможность
     *
     * Если opportunity.id является DEFAULT, будет сгенерирован новый уникальный ID.
     * Если opportunity.id уже существует, запись будет обновлена (upsert).
     *
     * @param opportunity возможность для сохранения
     * @return сгенерированный или существующий ID
     */
    suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId

    /**
     * Сохранить множество возможностей
     *
     * Batch операция для эффективного сохранения множества возможностей.
     * Должна быть атомарной (все или ничего) где это возможно.
     *
     * @param opportunities список возможностей для сохранения
     * @return список сгенерированных или существующих ID в том же порядке
     */
    suspend fun saveAll(opportunities: List<CexToCexArbitrageOpportunity>): List<ArbitrageOpportunityId>

    /**
     * Найти возможность по ID
     *
     * @param id идентификатор возможности
     * @return найденная возможность или null если не найдена
     */
    suspend fun findById(id: ArbitrageOpportunityId): CexToCexArbitrageOpportunity?

    /**
     * Получить все возможности
     *
     * @return список всех сохранённых возможностей (активных и завершённых)
     */
    suspend fun findAll(): List<CexToCexArbitrageOpportunity>

    /**
     * Найти только активные возможности
     *
     * Активная возможность - это возможность с endTimestamp == null.
     * Используется для получения текущих арбитражных ситуаций.
     *
     * @return список возможностей с endTimestamp == null
     */
    suspend fun findActive(): List<CexToCexArbitrageOpportunity>

    /**
     * Пометить возможность как завершённую
     *
     * Устанавливает endTimestamp для указанной возможности,
     * переводя её в завершённое состояние.
     *
     * Используется когда:
     * - Найден новый пересчёт и старые возможности больше не актуальны
     * - Арбитражная ситуация исчезла (цены выровнялись)
     * - Возможность была использована
     *
     * @param id идентификатор возможности
     * @param endTimestamp время завершения
     * @throws IllegalArgumentException если возможность не найдена
     */
    suspend fun markAsEnded(id: ArbitrageOpportunityId, endTimestamp: Timestamp)

    /**
     * Очистить все данные
     *
     * ВНИМАНИЕ: Деструктивная операция! Удаляет все возможности из репозитория.
     * Используется в тестах и для сброса состояния.
     */
    suspend fun clear()
}
