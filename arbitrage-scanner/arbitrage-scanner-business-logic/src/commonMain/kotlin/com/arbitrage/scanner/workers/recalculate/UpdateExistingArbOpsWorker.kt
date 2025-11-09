package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.UpdateArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::updateExistingArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.updateExistingArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Обновление существующих арбитражных возможностей в БД.
        Обновляет цены и spread, сохраняя id и startTimestamp.
    """.trimIndent()
    on { state == State.RUNNING && arbOpsToUpdate.isNotEmpty() }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val updateResult = arbOpRepo.update(
                UpdateArbOpRepoRequest.Items(arbOpsToUpdate)
            )

            when (updateResult) {
                is ArbOpRepoResponse.Multiple -> {
                    if (updateResult.arbOps.size != arbOpsToUpdate.size) {
                        logger.error(
                            "Несоответствие количества: ожидалось обновить ${arbOpsToUpdate.size}, " +
                            "обновлено ${updateResult.arbOps.size}"
                        )
                    }
                    logger.info("Успешно обновлено ${updateResult.arbOps.size} существующих возможностей")
                }

                is ArbOpRepoResponse.Single -> {
                    logger.info("Репозиторий вернул Single вместо Multiple для массового обновления")
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка обновления существующих возможностей: ${updateResult.errors}")
                    internalErrors.addAll(updateResult.errors)
                    state = State.FAILING
                }
            }
        }
    }
}
