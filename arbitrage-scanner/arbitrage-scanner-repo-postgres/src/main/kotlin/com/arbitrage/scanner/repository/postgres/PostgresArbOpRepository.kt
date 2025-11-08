package com.arbitrage.scanner.repository.postgres

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
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
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
        val itemWithId = if (arbOp.id.isDefault()) {
            arbOp.copy(id = ArbitrageOpportunityId(idGenerator()))
        } else {
            arbOp
        }
        // Генерируем начальный UUID токен для optimistic locking
        val initialLockToken = uuid4().toString()
        val entity = itemWithId.toEntity(lockToken = initialLockToken)

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

        ArbOpRepoResponse.Single(itemWithId)
    }

    private suspend fun createItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = dbQuery {
        val createdItems = arbOps.map { item ->
            val itemWithId = if (item.id.isDefault()) {
                item.copy(id = ArbitrageOpportunityId(idGenerator()))
            } else {
                item
            }
            itemWithId
        }

        ArbitrageOpportunitiesTable.batchInsert(createdItems) { item ->
            // Генерируем начальный UUID токен для каждого элемента
            val initialLockToken = uuid4().toString()
            val entity = item.toEntity(lockToken = initialLockToken)
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
        // Сначала читаем текущую запись
        val existingRow = ArbitrageOpportunitiesTable
            .selectAll()
            .where { ArbitrageOpportunitiesTable.id eq arbOp.id.value }
            .singleOrNull()

        if (existingRow == null) {
            return@dbQuery notFoundError(arbOp.id)
        }

        val currentLockToken = existingRow[ArbitrageOpportunitiesTable.lockToken]
        // Генерируем новый UUID токен для optimistic locking
        val newLockToken = uuid4().toString()
        val entity = arbOp.toEntity(lockToken = newLockToken)

        // Обновляем с проверкой lockToken (optimistic locking на основе UUID)
        val updatedCount = ArbitrageOpportunitiesTable.update({
            (ArbitrageOpportunitiesTable.id eq arbOp.id.value) and (ArbitrageOpportunitiesTable.lockToken eq currentLockToken)
        }) {
            it[tokenId] = entity.tokenId
            it[buyExchangeId] = entity.buyExchangeId
            it[sellExchangeId] = entity.sellExchangeId
            it[buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
            it[sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
            it[spread] = entity.spread
            it[startTimestamp] = entity.startTimestamp
            it[endTimestamp] = entity.endTimestamp
            it[lockToken] = newLockToken  // Устанавливаем новый UUID токен
        }

        if (updatedCount > 0) {
            // Читаем обновленную версию с новым lockToken
            val updated = ArbitrageOpportunitiesTable
                .selectAll()
                .where { ArbitrageOpportunitiesTable.id eq arbOp.id.value }
                .singleOrNull()

            if (updated != null) {
                ArbOpRepoResponse.Single(mapRowToEntity(updated).toDomain())
            } else {
                notFoundError(arbOp.id)
            }
        } else {
            versionConflictError(arbOp.id)
        }
    }

    private suspend fun updateItems(arbOps: List<CexToCexArbitrageOpportunity>): ArbOpRepoResponse = dbQuery {
        val errors = mutableListOf<InternalError>()
        val updated = mutableListOf<CexToCexArbitrageOpportunity>()

        arbOps.forEach { item ->
            // Для каждого элемента пробуем обновить
            val existingRow = ArbitrageOpportunitiesTable
                .selectAll()
                .where { ArbitrageOpportunitiesTable.id eq item.id.value }
                .singleOrNull()

            if (existingRow == null) {
                errors.add(createNotFoundError(item.id))
            } else {
                val currentLockToken = existingRow[ArbitrageOpportunitiesTable.lockToken]
                // Генерируем новый UUID токен для optimistic locking
                val newLockToken = uuid4().toString()
                val entity = item.toEntity(lockToken = newLockToken)

                val updatedCount = ArbitrageOpportunitiesTable.update({
                    (ArbitrageOpportunitiesTable.id eq item.id.value) and (ArbitrageOpportunitiesTable.lockToken eq currentLockToken)
                }) {
                    it[tokenId] = entity.tokenId
                    it[buyExchangeId] = entity.buyExchangeId
                    it[sellExchangeId] = entity.sellExchangeId
                    it[buyPriceRaw] = BigDecimal(entity.buyPriceRaw)
                    it[sellPriceRaw] = BigDecimal(entity.sellPriceRaw)
                    it[spread] = entity.spread
                    it[startTimestamp] = entity.startTimestamp
                    it[endTimestamp] = entity.endTimestamp
                    it[lockToken] = newLockToken  // Устанавливаем новый UUID токен
                }

                if (updatedCount > 0) {
                    updated.add(item)
                } else {
                    errors.add(createVersionConflictError(item.id))
                }
            }
        }

        if (errors.isNotEmpty()) {
            ArbOpRepoResponse.Error(errors)
        } else {
            ArbOpRepoResponse.Multiple(updated)
        }
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
        val errors = mutableListOf<InternalError>()
        val deleted = mutableListOf<CexToCexArbitrageOpportunity>()

        ids.forEach { id ->
            val row = ArbitrageOpportunitiesTable
                .selectAll()
                .where { ArbitrageOpportunitiesTable.id eq id.value }
                .singleOrNull()

            if (row != null) {
                val entity = mapRowToEntity(row)
                ArbitrageOpportunitiesTable.deleteWhere { ArbitrageOpportunitiesTable.id eq id.value }
                deleted.add(entity.toDomain())
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

    private suspend fun deleteAll(): ArbOpRepoResponse = dbQuery {
        val allRows = ArbitrageOpportunitiesTable.selectAll().toList()
        val allItems = allRows.map { mapRowToEntity(it).toDomain() }
        ArbitrageOpportunitiesTable.deleteAll()
        ArbOpRepoResponse.Multiple(allItems)
    }

    // ========== Private Methods: SEARCH ==========

    private suspend fun searchByCriteria(filter: ArbitrageOpportunityFilter): ArbOpRepoResponse = dbQuery {
        var query = ArbitrageOpportunitiesTable.selectAll()

        // Фильтр по токенам
        if (filter.cexTokenIds.isNotEmpty()) {
            val tokenIdValues = filter.cexTokenIds.map { it.value }
            query = query.andWhere { ArbitrageOpportunitiesTable.tokenId inList tokenIdValues }
        }

        // Фильтр по биржам (buy OR sell)
        if (filter.cexExchangeIds.isNotEmpty()) {
            val exchangeIdValues = filter.cexExchangeIds.map { it.value }
            query = query.andWhere {
                (ArbitrageOpportunitiesTable.buyExchangeId inList exchangeIdValues) or
                        (ArbitrageOpportunitiesTable.sellExchangeId inList exchangeIdValues)
            }
        }

        // Фильтр по минимальному спреду
        if (!filter.spread.isDefault()) {
            query = query.andWhere { ArbitrageOpportunitiesTable.spread greaterEq filter.spread.value }
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
