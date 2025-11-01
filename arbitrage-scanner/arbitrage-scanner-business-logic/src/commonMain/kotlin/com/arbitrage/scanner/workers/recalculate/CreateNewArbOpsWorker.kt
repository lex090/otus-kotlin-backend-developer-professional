package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.CreateArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::createNewArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.createNewArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Создание новых арбитражных возможностей в БД.
        Обрабатывает только те возможности, которые отсутствуют в БД.
    """.trimIndent()
    on { state == State.RUNNING && arbOpsToCreate.isNotEmpty() }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            val createResult = arbOpRepo.create(
                CreateArbOpRepoRequest.Items(arbOpsToCreate)
            )

            when (createResult) {
                is ArbOpRepoResponse.Multiple -> {
                    if (createResult.arbOps.size != arbOpsToCreate.size) {
                        logger.error(
                            "Несоответствие количества: ожидалось создать ${arbOpsToCreate.size}, " +
                            "создано ${createResult.arbOps.size}"
                        )
                    }
                    logger.info("Успешно создано ${createResult.arbOps.size} новых возможностей")
                }

                is ArbOpRepoResponse.Single -> {
                    logger.info("Репозиторий вернул Single вместо Multiple для массового создания")
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка создания новых возможностей: ${createResult.errors}")
                    internalErrors.addAll(createResult.errors)
                }
            }
        }
    }
}
