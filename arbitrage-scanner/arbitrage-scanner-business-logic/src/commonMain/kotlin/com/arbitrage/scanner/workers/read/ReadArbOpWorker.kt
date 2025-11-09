package com.arbitrage.scanner.workers.read

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.ReadArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::readArbOpWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.readArbOpWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Чтение арбитражной возможности из БД по ID.
        Использует валидированный ID из arbitrageOpportunityReadRequestValidated.
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val readResult = arbOpRepo.read(
                ReadArbOpRepoRequest.ById(arbitrageOpportunityReadRequestValidated)
            )

            when (readResult) {
                is ArbOpRepoResponse.Single -> {
                    arbitrageOpportunityReadResponse = readResult.arbOp
                    logger.info("Успешно прочитана арбитражная возможность с ID: ${arbitrageOpportunityReadRequestValidated.value}")
                }

                is ArbOpRepoResponse.Multiple -> {
                    logger.error("Репозиторий вернул Multiple вместо Single для чтения по ID")
                    if (readResult.arbOps.isNotEmpty()) {
                        arbitrageOpportunityReadResponse = readResult.arbOps.first()
                        logger.info("Использован первый элемент из Multiple ответа")
                    }
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка чтения арбитражной возможности: ${readResult.errors}")
                    internalErrors.addAll(readResult.errors)
                    state = State.FAILING
                }
            }
        }
    }
}
