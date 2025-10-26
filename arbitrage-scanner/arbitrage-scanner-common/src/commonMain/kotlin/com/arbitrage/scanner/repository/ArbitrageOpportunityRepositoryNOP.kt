package com.arbitrage.scanner.repository

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity

class ArbitrageOpportunityRepositoryNOP : ArbitrageOpportunityRepository {
    override suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun saveAll(opportunities: List<CexToCexArbitrageOpportunity>): List<ArbitrageOpportunityId> =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun findById(id: ArbitrageOpportunityId): CexToCexArbitrageOpportunity? =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun findAll(): List<CexToCexArbitrageOpportunity> =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun findActive(): List<CexToCexArbitrageOpportunity> =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun markAsEnded(id: ArbitrageOpportunityId, endTimestamp: Timestamp) =
        throw NotImplementedError("Not used in READ tests")

    override suspend fun clear() = throw NotImplementedError("Not used in READ tests")
}
