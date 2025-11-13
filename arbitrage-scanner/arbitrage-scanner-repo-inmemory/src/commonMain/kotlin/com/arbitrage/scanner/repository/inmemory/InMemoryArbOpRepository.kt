package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.LockToken
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.CreateArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.DeleteArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.ReadArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.SearchArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.UpdateArbOpRepoRequest
import com.arbitrage.scanner.repository.RepositoryException
import com.arbitrage.scanner.repository.tryExecute
import com.benasher44.uuid.uuid4
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.minutes

/**
 * In-memory реализация репозитория арбитражных возможностей.
 * Использует Cache4k для хранения данных с TTL 24 часа.
 * Автоматически генерирует UUID для новых записей.
 *
 * @param ttl Время жизни записей в кеше
 * @param maxSize Максимальный размер кеша
 * @param idGenerator Функция генерации ID для новых записей
 */
class InMemoryArbOpRepository(
    ttl: kotlin.time.Duration = 2.minutes,
    maxSize: Long = 10_000L,
    private val idGenerator: () -> String = { uuid4().toString() }
) : IArbOpRepository {

    private val cache: Cache<String, ArbitrageOpportunityEntity> = Cache.Builder<String, ArbitrageOpportunityEntity>()
        .expireAfterWrite(ttl)
        .maximumCacheSize(maxSize)
        .build()

    private val mutex = Mutex()

    override suspend fun create(request: CreateArbOpRepoRequest): ArbOpRepoResponse = tryExecute {
        when (request) {
            is CreateArbOpRepoRequest.Item -> createItem(request.arbOp)
            is CreateArbOpRepoRequest.Items -> createItems(request.arbOp)
        }
    }

    override suspend fun read(request: ReadArbOpRepoRequest): ArbOpRepoResponse = tryExecute {
        when (request) {
            is ReadArbOpRepoRequest.ById -> readById(request.id)
        }
    }

    override suspend fun update(request: UpdateArbOpRepoRequest): ArbOpRepoResponse = tryExecute {
        when (request) {
            is UpdateArbOpRepoRequest.Item -> updateItem(request.arbOp)
            is UpdateArbOpRepoRequest.Items -> updateItems(request.arbOp)
        }
    }

    override suspend fun delete(request: DeleteArbOpRepoRequest): ArbOpRepoResponse = tryExecute {
        when (request) {
            is DeleteArbOpRepoRequest.Item -> deleteItem(request.id)
            is DeleteArbOpRepoRequest.Items -> deleteItems(request.ids)
            is DeleteArbOpRepoRequest.All -> deleteAll()
        }
    }

    override suspend fun search(request: SearchArbOpRepoRequest): ArbOpRepoResponse = tryExecute {
        when (request) {
            is SearchArbOpRepoRequest.SearchCriteria -> searchByCriteria(request.arbOpFilter)
        }
    }

    // === Private methods ===

    private suspend fun createItem(arbOp: CexToCexArbitrageOpportunity): ArbOpRepoResponse = mutex.withLock {
        // Генерируем ID и lockToken если они по умолчанию
        val itemToCreate = arbOp.copy(
            id = if (arbOp.id.isDefault()) ArbitrageOpportunityId(idGenerator()) else arbOp.id,
            lockToken = if (arbOp.lockToken.isDefault()) LockToken(idGenerator()) else arbOp.lockToken
        )
        val entity = itemToCreate.toEntity()
        cache.put(entity.id, entity)
        ArbOpRepoResponse.Single(itemToCreate)
    }

    private suspend fun createItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = mutex.withLock {
        val createdItems = arbOps.map { item ->
            // Генерируем ID и lockToken для каждого элемента, если они по умолчанию
            item.copy(
                id = if (item.id.isDefault()) ArbitrageOpportunityId(idGenerator()) else item.id,
                lockToken = if (item.lockToken.isDefault()) LockToken(idGenerator()) else item.lockToken
            )
        }
        createdItems.forEach { item ->
            val entity = item.toEntity()
            cache.put(entity.id, entity)
        }
        ArbOpRepoResponse.Multiple(createdItems)
    }

    private suspend fun readById(id: ArbitrageOpportunityId): ArbOpRepoResponse = mutex.withLock {
        val entity = cache.get(id.value)
        if (entity != null) {
            ArbOpRepoResponse.Single(entity.toDomain())
        } else {
            notFoundError(id)
        }
    }

    private suspend fun updateItem(arbOp: CexToCexArbitrageOpportunity): ArbOpRepoResponse = mutex.withLock {
        val existing = cache.get(arbOp.id.value) ?: return@withLock notFoundError(arbOp.id)

        // Проверка оптимистичной блокировки - lockToken должен совпадать
        if (existing.lockToken != arbOp.lockToken.value) {
            return@withLock versionConflictError(arbOp.id)
        }

        // Генерируем новый lockToken
        val updatedItem = arbOp.copy(lockToken = LockToken(idGenerator()))
        val entity = updatedItem.toEntity()
        cache.put(entity.id, entity)
        ArbOpRepoResponse.Single(updatedItem)
    }

    private suspend fun updateItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = mutex.withLock {
        // Сначала проверяем все элементы на существование и версии
        arbOps.forEach { item ->
            val existing = cache.get(item.id.value) ?: throw RepositoryException(createNotFoundError(item.id))
            // Проверка оптимистичной блокировки
            if (existing.lockToken != item.lockToken.value) {
                throw RepositoryException(createVersionConflictError(item.id))
            }
        }

        // Если все проверки прошли, обновляем элементы
        val updated = arbOps.map { item ->
            // Генерируем новый lockToken для каждого элемента
            val updatedItem = item.copy(lockToken = LockToken(idGenerator()))
            val entity = updatedItem.toEntity()
            cache.put(entity.id, entity)
            updatedItem
        }

        ArbOpRepoResponse.Multiple(updated)
    }

    private suspend fun deleteItem(id: ArbitrageOpportunityId): ArbOpRepoResponse = mutex.withLock {
        val existing = cache.get(id.value)
        if (existing != null) {
            cache.invalidate(id.value)
            ArbOpRepoResponse.Single(existing.toDomain())
        } else {
            notFoundError(id)
        }
    }

    private suspend fun deleteItems(ids: List<ArbitrageOpportunityId>): ArbOpRepoResponse = mutex.withLock {
        // Сначала проверяем все элементы на существование и собираем их
        val toDelete = ids.map { id ->
            // При первой ошибке выбрасываем исключение - это обеспечит атомарность
            cache.get(id.value) ?: throw RepositoryException(createNotFoundError(id))
        }

        // Если все элементы существуют, удаляем их
        val deleted = toDelete.map { entity ->
            cache.invalidate(entity.id)
            entity.toDomain()
        }

        ArbOpRepoResponse.Multiple(deleted)
    }

    private suspend fun deleteAll(): ArbOpRepoResponse = mutex.withLock {
        val allItems = cache.asMap().values.map { it.toDomain() }
        cache.invalidateAll()
        ArbOpRepoResponse.Multiple(allItems)
    }

    private suspend fun searchByCriteria(filter: ArbitrageOpportunityFilter): ArbOpRepoResponse = mutex.withLock {
        val filtered = cache.asMap().values.asSequence()
            // Фильтр по токенам
            .filter { entity ->
                filter.cexTokenIds.value.isEmpty() || filter.cexTokenIds.value.any { it.value == entity.tokenId }
            }
            // Фильтр по биржам покупки
            .filter { entity ->
                filter.buyExchangeIds.isEmpty() || filter.buyExchangeIds.any { it.value == entity.buyExchangeId }
            }
            // Фильтр по биржам продажи
            .filter { entity ->
                filter.sellExchangeIds.isEmpty() || filter.sellExchangeIds.any { it.value == entity.sellExchangeId }
            }
            // Фильтр по минимальному спреду
            .filter { entity ->
                filter.minSpread.isNone() || entity.spread >= filter.minSpread.value
            }
            // Фильтр по максимальному спреду
            .filter { entity ->
                filter.maxSpread?.let { entity.spread <= it.value } ?: true
            }
            // Фильтр по статусу
            .filter { entity ->
                when (filter.status) {
                    ArbitrageOpportunityStatus.ACTIVE -> entity.endTimestamp == null
                    ArbitrageOpportunityStatus.INACTIVE -> entity.endTimestamp != null
                    ArbitrageOpportunityStatus.ALL -> true
                    ArbitrageOpportunityStatus.NONE -> error("Status filter NONE is not supported in search. This is a validation error.")
                }
            }
            // Фильтр по временному диапазону создания (startTimestamp)
            .filter { entity ->
                filter.startTimestampFrom?.let { entity.startTimestamp >= it.value } ?: true
            }
            .filter { entity ->
                filter.startTimestampTo?.let { entity.startTimestamp <= it.value } ?: true
            }
            // Фильтр по временному диапазону завершения (endTimestamp)
            .filter { entity ->
                filter.endTimestampFrom?.let { from ->
                    entity.endTimestamp?.let { it >= from.value } ?: false
                } ?: true
            }
            .filter { entity ->
                filter.endTimestampTo?.let { to ->
                    entity.endTimestamp?.let { it <= to.value } ?: false
                } ?: true
            }
            .map { it.toDomain() }
            .toList()

        ArbOpRepoResponse.Multiple(filtered)
    }

    /**
     * Создает ошибку "не найдено" для указанного ID
     */
    private fun notFoundError(id: ArbitrageOpportunityId): ArbOpRepoResponse {
        return ArbOpRepoResponse.Error(listOf(createNotFoundError(id)))
    }

    /**
     * Создает InternalError с кодом "не найдено"
     */
    private fun createNotFoundError(id: ArbitrageOpportunityId): InternalError {
        return InternalError(
            code = "repo-not-found",
            group = "repository",
            field = "id",
            message = "Arbitrage opportunity with id ${id.value} not found"
        )
    }

    /**
     * Создает ошибку version conflict для указанного ID
     */
    private fun versionConflictError(id: ArbitrageOpportunityId): ArbOpRepoResponse {
        return ArbOpRepoResponse.Error(listOf(createVersionConflictError(id)))
    }

    /**
     * Создает InternalError с кодом "version conflict"
     */
    private fun createVersionConflictError(id: ArbitrageOpportunityId): InternalError {
        return InternalError(
            code = "repo-version-conflict",
            group = "repository",
            field = "lockVersion",
            message = "Version conflict: arbitrage opportunity with id ${id.value} was modified by another transaction"
        )
    }
}
