package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fail
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.SearchArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::loadActiveArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.loadActiveArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Загрузка активных арбитражных возможностей из БД.
        Фильтрует только те, у которых endTimestamp == null.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val searchResult = arbOpRepo.search(
                SearchArbOpRepoRequest.SearchCriteria(
                    ArbitrageOpportunityFilter(status = ArbitrageOpportunityStatus.ALL)
                )
            )

            when (searchResult) {
                is ArbOpRepoResponse.Multiple -> {
                    val active = searchResult.arbOps.filter { it.endTimestamp == null }
                    existingActiveArbOps.addAll(active)
                    logger.info("Загружено ${active.size} активных возможностей из БД (всего: ${searchResult.arbOps.size})")
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка загрузки существующих возможностей: ${searchResult.errors}")
                    fail(searchResult.errors)
                }

                is ArbOpRepoResponse.Single -> {
                    logger.error("Неожиданный Single вариант для search операции")
                    error("Search не должен возвращать Single")
                }
            }
        }
    }
}
