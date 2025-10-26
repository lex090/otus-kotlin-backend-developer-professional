package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.repository.ArbitrageOpportunityRepository
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/**
 * In-memory реализация репозитория арбитражных возможностей
 *
 * Эта реализация использует thread-safe структуры данных для корректной
 * работы в многопоточных средах:
 * - Thread-safe Map для хранения возможностей
 * - Атомарный счётчик для генерации уникальных ID
 *
 * Используется для:
 * - Быстрого прототипирования и разработки
 * - Unit и интеграционного тестирования
 * - Временного хранения данных в рамках одной сессии приложения
 *
 * ВАЖНО: Данные НЕ персистентны - при перезапуске приложения теряются.
 *
 * @see ArbitrageOpportunityRepository
 */
class InMemoryArbitrageOpportunityRepository : ArbitrageOpportunityRepository {

    /**
     * Thread-safe хранилище арбитражных возможностей
     * Ключ: ArbitrageOpportunityId, Значение: CexToCexArbitrageOpportunity
     */
    private val opportunities = atomic(emptyMap<ArbitrageOpportunityId, CexToCexArbitrageOpportunity>())

    /**
     * Атомарный счётчик для генерации уникальных ID
     * Стартует с 1 для читабельности ID (arb-1, arb-2, ...)
     */
    private val idGenerator = atomic(0L)

    /**
     * Сохраняет арбитражную возможность в репозиторий
     *
     * Реализует upsert логику:
     * - Если id.isDefault(), генерируется новый уникальный ID
     * - Если id уже существует, запись перезаписывается
     *
     * Thread-safe операция.
     *
     * @param opportunity арбитражная возможность для сохранения
     * @return ID сохранённой возможности (сгенерированный или существующий)
     */
    override suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId {
        val id = if (opportunity.id.isDefault()) {
            generateId()
        } else {
            opportunity.id
        }

        val withId = opportunity.copy(id = id)

        opportunities.update { current ->
            current + (id to withId)
        }

        return id
    }

    /**
     * Batch операция сохранения множества возможностей
     *
     * Более эффективна чем множественные вызовы save() за счёт
     * одной атомарной операции обновления хранилища.
     *
     * Thread-safe операция.
     *
     * @param opportunities список арбитражных возможностей для сохранения
     * @return список ID в том же порядке что и входные возможности
     */
    override suspend fun saveAll(opportunities: List<CexToCexArbitrageOpportunity>): List<ArbitrageOpportunityId> {
        if (opportunities.isEmpty()) {
            return emptyList()
        }

        val withIds = opportunities.map { opportunity ->
            val id = if (opportunity.id.isDefault()) {
                generateId()
            } else {
                opportunity.id
            }
            opportunity.copy(id = id)
        }

        this.opportunities.update { current ->
            current + withIds.associateBy { it.id }
        }

        return withIds.map { it.id }
    }

    /**
     * Поиск арбитражной возможности по ID
     *
     * Thread-safe операция чтения.
     *
     * @param id идентификатор возможности
     * @return найденная возможность или null если не существует
     */
    override suspend fun findById(id: ArbitrageOpportunityId): CexToCexArbitrageOpportunity? {
        return opportunities.value[id]
    }

    /**
     * Получение всех сохранённых арбитражных возможностей
     *
     * Возвращает как активные (endTimestamp == null),
     * так и завершённые возможности (endTimestamp != null).
     *
     * Thread-safe операция чтения.
     *
     * @return список всех возможностей (может быть пустым)
     */
    override suspend fun findAll(): List<CexToCexArbitrageOpportunity> {
        return opportunities.value.values.toList()
    }

    /**
     * Поиск только активных арбитражных возможностей
     *
     * Активная возможность - возможность с endTimestamp == null,
     * то есть ещё не помеченная как завершённая.
     *
     * Используется для:
     * - Получения текущих арбитражных ситуаций
     * - Отображения активных возможностей пользователю
     * - Пометки старых возможностей при пересчёте
     *
     * Thread-safe операция чтения.
     *
     * @return список активных возможностей (может быть пустым)
     */
    override suspend fun findActive(): List<CexToCexArbitrageOpportunity> {
        return opportunities.value.values.filter { it.endTimestamp == null }
    }

    /**
     * Помечает арбитражную возможность как завершённую
     *
     * Устанавливает endTimestamp для возможности, переводя её из
     * активного состояния в завершённое.
     *
     * Используется когда:
     * - Найден новый пересчёт и старые возможности больше не актуальны
     * - Арбитражная ситуация исчезла (цены выровнялись)
     * - Возможность была использована
     *
     * Thread-safe операция.
     *
     * @param id идентификатор возможности для завершения
     * @param endTimestamp время завершения возможности
     */
    override suspend fun markAsEnded(id: ArbitrageOpportunityId, endTimestamp: Timestamp) {
        val opportunity = opportunities.value[id]
            ?: return // Если возможность не найдена, просто ничего не делаем

        val updated = opportunity.copy(endTimestamp = endTimestamp)

        opportunities.update { current ->
            current + (id to updated)
        }
    }

    /**
     * Удаляет все арбитражные возможности из репозитория
     *
     * ВНИМАНИЕ: Деструктивная операция!
     * Используется в тестах и для сброса состояния системы.
     * Также сбрасывает счётчик ID в начальное состояние.
     *
     * Thread-safe операция.
     */
    override suspend fun clear() {
        opportunities.value = emptyMap()
        idGenerator.value = 0L
    }

    /**
     * Генерирует уникальный ID для новой арбитражной возможности
     *
     * Формат: "arb-{counter}" где counter - последовательный номер
     * Примеры: arb-1, arb-2, arb-3, ...
     *
     * Thread-safe операция за счёт использования атомарного счётчика.
     *
     * @return новый уникальный ArbitrageOpportunityId
     */
    private fun generateId(): ArbitrageOpportunityId {
        val nextId = idGenerator.incrementAndGet()
        return ArbitrageOpportunityId("arb-$nextId")
    }
}
