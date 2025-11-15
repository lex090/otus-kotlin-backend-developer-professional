package com.arbitrage.scanner.repository.postgres

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunityFilter
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
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

/**
 * PostgreSQL реализация репозитория арбитражных возможностей.
 *
 * Использует Exposed ORM для работы с БД и HikariCP для connection pooling.
 * Схема БД управляется через Liquibase миграции.
 *
 * @param database Database instance (уже инициализирован через DatabaseFactory)
 * @param idGenerator Функция генерации ID для новых записей
 */
class PostgresArbOpRepository(
    private val database: Database,
    private val idGenerator: () -> String = { uuid4().toString() }
) : IArbOpRepository {

    /**
     * Helper метод для выполнения DB операций в suspend transaction
     */
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

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

    // ========== Private Methods: CREATE ==========

    private suspend fun createItem(arbOp: CexToCexArbitrageOpportunity): ArbOpRepoResponse = dbQuery {
        // Генерируем ID и lockToken если они по умолчанию
        val itemToCreate = arbOp.copy(
            id = if (arbOp.id.isDefault()) ArbitrageOpportunityId(idGenerator()) else arbOp.id,
            lockToken = if (arbOp.lockToken.isNone()) LockToken(idGenerator()) else arbOp.lockToken
        )
        val entity = itemToCreate.toEntity()

        ArbitrageOpportunitiesTable.insert {
            it[id] = entity.id
            it[tokenId] = entity.tokenId
            it[buyExchangeId] = entity.buyExchangeId
            it[sellExchangeId] = entity.sellExchangeId
            it[buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
            it[sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
            it[spread] = entity.spread
            it[startTimestamp] = entity.startTimestamp
            it[endTimestamp] = entity.endTimestamp
            it[lockToken] = entity.lockToken
        }

        ArbOpRepoResponse.Single(itemToCreate)
    }

    private suspend fun createItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = dbQuery {
        // Генерируем ID и lockToken для каждого элемента, если они по умолчанию
        val createdItems = arbOps.map { item ->
            item.copy(
                id = if (item.id.isDefault()) ArbitrageOpportunityId(idGenerator()) else item.id,
                lockToken = if (item.lockToken.isNone()) LockToken(idGenerator()) else item.lockToken
            )
        }

        ArbitrageOpportunitiesTable.batchInsert(createdItems) { item ->
            val entity = item.toEntity()
            this[ArbitrageOpportunitiesTable.id] = entity.id
            this[ArbitrageOpportunitiesTable.tokenId] = entity.tokenId
            this[ArbitrageOpportunitiesTable.buyExchangeId] = entity.buyExchangeId
            this[ArbitrageOpportunitiesTable.sellExchangeId] = entity.sellExchangeId
            this[ArbitrageOpportunitiesTable.buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
            this[ArbitrageOpportunitiesTable.sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
            this[ArbitrageOpportunitiesTable.spread] = entity.spread
            this[ArbitrageOpportunitiesTable.startTimestamp] = entity.startTimestamp
            this[ArbitrageOpportunitiesTable.endTimestamp] = entity.endTimestamp
            this[ArbitrageOpportunitiesTable.lockToken] = entity.lockToken
        }

        ArbOpRepoResponse.Multiple(createdItems)
    }

    // ========== Private Methods: READ ==========

    private suspend fun readById(id: ArbitrageOpportunityId): ArbOpRepoResponse = dbQuery {
        val row = ArbitrageOpportunitiesTable
            .selectAll()
            .where { ArbitrageOpportunitiesTable.id eq id.value }
            .singleOrNull()

        if (row != null) {
            val entity = mapRowToEntity(row)
            ArbOpRepoResponse.Single(entity.toDomain())
        } else {
            notFoundError(id)
        }
    }

    // ========== Private Methods: UPDATE ==========

    private suspend fun updateItem(arbOp: CexToCexArbitrageOpportunity): ArbOpRepoResponse = dbQuery {
        // Генерируем новый lockToken для optimistic locking
        val newLockToken = idGenerator()
        val updatedItem = arbOp.copy(lockToken = LockToken(newLockToken))
        val entity = updatedItem.toEntity()

        // Обновляем с проверкой lockToken (optimistic locking)
        val updatedCount = ArbitrageOpportunitiesTable.update({
            (ArbitrageOpportunitiesTable.id eq arbOp.id.value) and
            (ArbitrageOpportunitiesTable.lockToken eq arbOp.lockToken.value)
        }) {
            it[tokenId] = entity.tokenId
            it[buyExchangeId] = entity.buyExchangeId
            it[sellExchangeId] = entity.sellExchangeId
            it[buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
            it[sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
            it[spread] = entity.spread
            it[startTimestamp] = entity.startTimestamp
            it[endTimestamp] = entity.endTimestamp
            it[lockToken] = newLockToken  // Устанавливаем новый lockToken
        }

        if (updatedCount > 0) {
            // Возвращаем обновленную модель с новым lockToken
            ArbOpRepoResponse.Single(updatedItem)
        } else {
            // Проверяем, существует ли запись с таким ID
            val exists = ArbitrageOpportunitiesTable
                .selectAll()
                .where { ArbitrageOpportunitiesTable.id eq arbOp.id.value }
                .singleOrNull() != null

            if (exists) {
                versionConflictError(arbOp.id)
            } else {
                notFoundError(arbOp.id)
            }
        }
    }

    private suspend fun updateItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = dbQuery {
        val updated = mutableListOf<CexToCexArbitrageOpportunity>()

        // Обновляем каждую запись с проверкой lockToken
        arbOps.forEach { item ->
            // Генерируем новый lockToken для optimistic locking
            val newLockToken = idGenerator()
            val updatedItem = item.copy(lockToken = LockToken(newLockToken))
            val entity = updatedItem.toEntity()

            val updatedCount = ArbitrageOpportunitiesTable.update({
                (ArbitrageOpportunitiesTable.id eq item.id.value) and
                (ArbitrageOpportunitiesTable.lockToken eq item.lockToken.value)
            }) {
                it[tokenId] = entity.tokenId
                it[buyExchangeId] = entity.buyExchangeId
                it[sellExchangeId] = entity.sellExchangeId
                it[buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
                it[sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
                it[spread] = entity.spread
                it[startTimestamp] = entity.startTimestamp
                it[endTimestamp] = entity.endTimestamp
                it[lockToken] = newLockToken
            }

            // При конфликте версий или отсутствии записи выбрасываем исключение
            // Это откатит всю транзакцию
            if (updatedCount == 0) {
                // Проверяем, существует ли запись с таким ID
                val exists = ArbitrageOpportunitiesTable
                    .selectAll()
                    .where { ArbitrageOpportunitiesTable.id eq item.id.value }
                    .singleOrNull() != null

                if (exists) {
                    throw RepositoryException(createVersionConflictError(item.id))
                } else {
                    throw RepositoryException(createNotFoundError(item.id))
                }
            }

            updated.add(updatedItem)
        }

        ArbOpRepoResponse.Multiple(updated)
    }

    // ========== Private Methods: DELETE ==========

    private suspend fun deleteItem(id: ArbitrageOpportunityId): ArbOpRepoResponse = dbQuery {
        val row = ArbitrageOpportunitiesTable
            .selectAll()
            .where { ArbitrageOpportunitiesTable.id eq id.value }
            .singleOrNull()

        if (row != null) {
            val entity = mapRowToEntity(row)
            ArbitrageOpportunitiesTable.deleteWhere { ArbitrageOpportunitiesTable.id eq id.value }
            ArbOpRepoResponse.Single(entity.toDomain())
        } else {
            notFoundError(id)
        }
    }

    private suspend fun deleteItems(ids: List<ArbitrageOpportunityId>): ArbOpRepoResponse = dbQuery {
        // Получаем все записи одним запросом вместо N отдельных SELECT
        val idValues = ids.map { it.value }
        val rows = ArbitrageOpportunitiesTable
            .selectAll()
            .where { ArbitrageOpportunitiesTable.id inList idValues }
            .toList()

        // Создаем map для быстрого поиска
        val rowsMap = rows.associateBy { it[ArbitrageOpportunitiesTable.id] }

        // Проверяем наличие всех ID
        val missingIds = ids.filter { !rowsMap.containsKey(it.value) }
        if (missingIds.isNotEmpty()) {
            throw RepositoryException(createNotFoundError(missingIds.first()))
        }

        // Маппим в domain объекты
        val deleted = rows.map { mapRowToEntity(it).toDomain() }

        // Удаляем все записи одним запросом
        ArbitrageOpportunitiesTable.deleteWhere { ArbitrageOpportunitiesTable.id inList idValues }

        ArbOpRepoResponse.Multiple(deleted)
    }

    private suspend fun deleteAll(): ArbOpRepoResponse = dbQuery {
        val allRows = ArbitrageOpportunitiesTable.selectAll().toList()
        val allItems = allRows.map { mapRowToEntity(it).toDomain() }
        ArbitrageOpportunitiesTable.deleteAll()
        ArbOpRepoResponse.Multiple(allItems)
    }

    // ========== Private Methods: SEARCH ==========

    private suspend fun searchByCriteria(filter: CexToCexArbitrageOpportunityFilter): ArbOpRepoResponse = dbQuery {
        var query = ArbitrageOpportunitiesTable.selectAll()

        // Фильтр по токенам
        if (filter.cexTokenIdsFilter.isNotNone() && filter.cexTokenIdsFilter.value.isNotEmpty()) {
            val tokenIdValues = filter.cexTokenIdsFilter.value
                .filter { it.isNotNone() }
                .map { it.value }
            if (tokenIdValues.isNotEmpty()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.tokenId inList tokenIdValues }
            }
        }

        // Фильтр по биржам покупки
        if (filter.buyExchangeIds.isNotNone() && filter.buyExchangeIds.value.isNotEmpty()) {
            val buyExchangeIdValues = filter.buyExchangeIds.value
                .filter { it.isNotDefault() }
                .map { it.value }
            if (buyExchangeIdValues.isNotEmpty()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.buyExchangeId inList buyExchangeIdValues }
            }
        }

        // Фильтр по биржам продажи
        if (filter.sellExchangeIds.isNotNone() && filter.sellExchangeIds.value.isNotEmpty()) {
            val sellExchangeIdValues = filter.sellExchangeIds.value
                .filter { it.isNotDefault() }
                .map { it.value }
            if (sellExchangeIdValues.isNotEmpty()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.sellExchangeId inList sellExchangeIdValues }
            }
        }

        // Фильтр по минимальному спреду
        if (filter.minSpread.isNotNone()) {
            query = query.andWhere { ArbitrageOpportunitiesTable.spread greaterEq filter.minSpread.value }
        }

        // Фильтр по максимальному спреду
        filter.maxSpread?.let { maxSpread ->
            if (maxSpread.isNotNone()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.spread lessEq maxSpread.value }
            }
        }

        // Фильтр по статусу
        when (filter.status) {
            ArbitrageOpportunityStatus.ACTIVE -> {
                query = query.andWhere { ArbitrageOpportunitiesTable.endTimestamp.isNull() }
            }
            ArbitrageOpportunityStatus.INACTIVE -> {
                query = query.andWhere { ArbitrageOpportunitiesTable.endTimestamp.isNotNull() }
            }
            ArbitrageOpportunityStatus.ALL -> Unit
            ArbitrageOpportunityStatus.NONE -> Unit
        }

        // Фильтр по времени начала (startTimestamp >= filter.startTimestamp)
        filter.startTimestamp?.let { filterTime ->
            if (filterTime.isNotNone()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.startTimestamp greaterEq filterTime.value }
            }
        }

        // Фильтр по времени окончания (endTimestamp <= filter.endTimestamp)
        filter.endTimestamp?.let { filterTime ->
            if (filterTime.isNotNone()) {
                query = query.andWhere { ArbitrageOpportunitiesTable.endTimestamp lessEq filterTime.value }
            }
        }

        val results = query.map { mapRowToEntity(it).toDomain() }
        ArbOpRepoResponse.Multiple(results)
    }

    // ========== Helper Methods ==========

    /**
     * Маппинг ResultRow в ArbitrageOpportunityEntity
     */
    private fun mapRowToEntity(row: ResultRow): ArbitrageOpportunityEntity {
        return ArbitrageOpportunityEntity(
            id = row[ArbitrageOpportunitiesTable.id],
            tokenId = row[ArbitrageOpportunitiesTable.tokenId],
            buyExchangeId = row[ArbitrageOpportunitiesTable.buyExchangeId],
            sellExchangeId = row[ArbitrageOpportunitiesTable.sellExchangeId],
            buyPriceRaw = row[ArbitrageOpportunitiesTable.buyPriceRaw].toString(),
            sellPriceRaw = row[ArbitrageOpportunitiesTable.sellPriceRaw].toString(),
            spread = row[ArbitrageOpportunitiesTable.spread],
            startTimestamp = row[ArbitrageOpportunitiesTable.startTimestamp],
            endTimestamp = row[ArbitrageOpportunitiesTable.endTimestamp],
            lockToken = row[ArbitrageOpportunitiesTable.lockToken]
        )
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
