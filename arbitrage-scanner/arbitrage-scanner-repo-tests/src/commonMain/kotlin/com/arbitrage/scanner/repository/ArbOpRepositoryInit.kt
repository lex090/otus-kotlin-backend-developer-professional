package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import kotlinx.coroutines.runBlocking

class ArbOpRepositoryInit(
    repo: IArbOpRepository,
    private val initItems: List<CexToCexArbitrageOpportunity>,
) : IArbOpRepository by repo {
    init {
        runBlocking {
            repo.create(IArbOpRepository.CreateArbOpRepoRequest.Items(initItems))
        }
    }
}
