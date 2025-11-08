package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.CreateArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.DeleteArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.ReadArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.SearchArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.UpdateArbOpRepoRequest
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
        val itemWithId = if (arbOp.id.isDefault()) {
            arbOp.copy(id = ArbitrageOpportunityId(idGenerator()))
        } else {
            arbOp
        }
        val entity = itemWithId.toEntity()
        cache.put(entity.id, entity)
        ArbOpRepoResponse.Single(itemWithId)
    }

    private suspend fun createItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = mutex.withLock {
        val createdItems = arbOps.map { item ->
            val itemWithId = if (item.id.isDefault()) {
                item.copy(id = ArbitrageOpportunityId(idGenerator()))
            } else {
                item
            }
            val entity = itemWithId.toEntity()
            cache.put(entity.id, entity)
            itemWithId
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
        val existing = cache.get(arbOp.id.value)
        if (existing != null) {
            val entity = arbOp.toEntity()
            cache.put(entity.id, entity)
            ArbOpRepoResponse.Single(arbOp)
        } else {
            notFoundError(arbOp.id)
        }
    }

    private suspend fun updateItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = mutex.withLock {
        val errors = mutableListOf<InternalError>()
        val updated = mutableListOf<CexToCexArbitrageOpportunity>()

        arbOps.forEach { item ->
            val existing = cache.get(item.id.value)
            if (existing != null) {
                val entity = item.toEntity()
                cache.put(entity.id, entity)
                updated.add(item)
            } else {
                errors.add(createNotFoundError(item.id))
            }
        }

        if (errors.isNotEmpty()) {
            ArbOpRepoResponse.Error(errors)
        } else {
            ArbOpRepoResponse.Multiple(updated)
        }
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
        val errors = mutableListOf<InternalError>()
        val deleted = mutableListOf<CexToCexArbitrageOpportunity>()

        ids.forEach { id ->
            val existing = cache.get(id.value)
            if (existing != null) {
                cache.invalidate(id.value)
                deleted.add(existing.toDomain())
            } else {
                errors.add(createNotFoundError(id))
            }
        }

        if (errors.isNotEmpty()) {
            ArbOpRepoResponse.Error(errors)
        } else {
            ArbOpRepoResponse.Multiple(deleted)
        }
    }

    private suspend fun deleteAll(): ArbOpRepoResponse = mutex.withLock {
        val allItems = cache.asMap().values.map { it.toDomain() }
        cache.invalidateAll()
        ArbOpRepoResponse.Multiple(allItems)
    }

    private suspend fun searchByCriteria(filter: ArbitrageOpportunityFilter): ArbOpRepoResponse = mutex.withLock {
        val filtered = cache.asMap().values.asSequence()
            .filter { entity ->
                filter.cexTokenIds.isEmpty() || filter.cexTokenIds.any { it.value == entity.tokenId }
            }
            .filter { entity ->
                if (filter.cexExchangeIds.isEmpty()) {
                    true
                } else {
                    filter.cexExchangeIds.any { it.value == entity.buyExchangeId || it.value == entity.sellExchangeId }
                }
            }
            .filter { entity ->
                filter.spread.isDefault() || entity.spread >= filter.spread.value
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
}
