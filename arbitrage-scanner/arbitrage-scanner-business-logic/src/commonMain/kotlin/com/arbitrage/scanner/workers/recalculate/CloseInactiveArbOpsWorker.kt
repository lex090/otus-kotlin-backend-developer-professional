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

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::closeInactiveArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.closeInactiveArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Закрытие неактуальных арбитражных возможностей.
        Устанавливает endTimestamp для возможностей, которых нет в новых данных.
    """.trimIndent()
    on { state == State.RUNNING && arbOpsToClose.isNotEmpty() }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val closeResult = arbOpRepo.update(
                UpdateArbOpRepoRequest.Items(arbOpsToClose)
            )

            when (closeResult) {
                is ArbOpRepoResponse.Multiple -> {
                    if (closeResult.arbOps.size != arbOpsToClose.size) {
                        logger.error(
                            "Несоответствие количества: ожидалось закрыть ${arbOpsToClose.size}, " +
                            "закрыто ${closeResult.arbOps.size}"
                        )
                    }
                    logger.info("Успешно закрыто ${closeResult.arbOps.size} неактуальных возможностей")
                }

                is ArbOpRepoResponse.Single -> {
                    logger.info("Репозиторий вернул Single вместо Multiple для массового закрытия")
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка закрытия неактуальных возможностей: ${closeResult.errors}")
                    internalErrors.addAll(closeResult.errors)
                }
            }
        }
    }
}
