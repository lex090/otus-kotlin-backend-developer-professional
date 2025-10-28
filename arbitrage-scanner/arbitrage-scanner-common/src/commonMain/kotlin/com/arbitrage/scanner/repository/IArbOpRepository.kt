package com.arbitrage.scanner.repository

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity

interface IArbOpRepository {
    suspend fun create(request: CreateArbOpRepoRequest): ArbOpRepoResponse
    suspend fun read(request: ReadArbOpRepoRequest): ArbOpRepoResponse
    suspend fun update(request: UpdateArbOpRepoRequest): ArbOpRepoResponse
    suspend fun delete(request: DeleteArbOpRepoRequest): ArbOpRepoResponse
    suspend fun search(request: SearchArbOpRepoRequest): ArbOpRepoResponse

    sealed interface CreateArbOpRepoRequest {
        data class Item(val arbOp: CexToCexArbitrageOpportunity) : CreateArbOpRepoRequest
        data class Items(val arbOp: List<CexToCexArbitrageOpportunity>) : CreateArbOpRepoRequest
    }

    sealed interface ReadArbOpRepoRequest {
        data class ById(val id: ArbitrageOpportunityId) : ReadArbOpRepoRequest
    }

    sealed interface UpdateArbOpRepoRequest {
        data class Item(val arbOp: CexToCexArbitrageOpportunity) : UpdateArbOpRepoRequest
        data class Items(val arbOp: List<CexToCexArbitrageOpportunity>) : UpdateArbOpRepoRequest
    }

    sealed interface DeleteArbOpRepoRequest {
        data class Item(val id: ArbitrageOpportunityId) : DeleteArbOpRepoRequest
        data class Items(val ids: List<ArbitrageOpportunityId>) : DeleteArbOpRepoRequest
        data object All : DeleteArbOpRepoRequest
    }

    sealed interface SearchArbOpRepoRequest {
        data class SearchCriteria(
            val arbOpFilter: ArbitrageOpportunityFilter,
        ) : SearchArbOpRepoRequest
    }

    sealed interface ArbOpRepoResponse {
        data class Single(val arbOp: CexToCexArbitrageOpportunity) : ArbOpRepoResponse
        data class Multiple(val arbOps: List<CexToCexArbitrageOpportunity>) : ArbOpRepoResponse
        data class Error(val errors: List<InternalError>) : ArbOpRepoResponse
    }
}