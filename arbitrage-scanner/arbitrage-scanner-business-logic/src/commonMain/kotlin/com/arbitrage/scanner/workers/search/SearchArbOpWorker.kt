package com.arbitrage.scanner.workers.search

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.SearchArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::searchArbOpWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.searchArbOpWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Поиск арбитражных возможностей в БД по фильтру.
        Использует валидированный фильтр из arbitrageOpportunitySearchRequestValidated.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val searchResult = arbOpRepo.search(
                SearchArbOpRepoRequest.SearchCriteria(arbitrageOpportunitySearchRequestValidated)
            )

            when (searchResult) {
                is ArbOpRepoResponse.Multiple -> {
                    arbitrageOpportunitySearchResponse.clear()
                    arbitrageOpportunitySearchResponse.addAll(searchResult.arbOps)
                    logger.info("Найдено ${searchResult.arbOps.size} арбитражных возможностей")
                }

                is ArbOpRepoResponse.Single -> {
                    arbitrageOpportunitySearchResponse.clear()
                    arbitrageOpportunitySearchResponse.add(searchResult.arbOp)
                    logger.info("Репозиторий вернул Single вместо Multiple для поиска, добавлен 1 элемент")
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка поиска арбитражных возможностей: ${searchResult.errors}")
                    internalErrors.addAll(searchResult.errors)
                    state = State.FAILING
                }
            }
        }
    }
}
