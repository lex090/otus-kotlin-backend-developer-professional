package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.repository.IArbOpRepository.ArbOpRepoResponse
import com.arbitrage.scanner.repository.IArbOpRepository.CreateArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.SearchArbOpRepoRequest
import com.arbitrage.scanner.repository.IArbOpRepository.UpdateArbOpRepoRequest
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction
import kotlin.time.Clock

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::updateArbOpsWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.updateArbOpsWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Обновление арбитражных возможностей в БД:
        - Создание новых возможностей
        - Обновление существующих возможностей
        - Закрытие неактуальных возможностей (установка endTimestamp)
    """.trimIndent()
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            if (arbOps.isEmpty()) {
                logger.info("Нет новых арбитражных возможностей для обновления")
                return@doWithLogging
            }

            // 1. Получаем все существующие арбитражные возможности из БД
            val searchResult = arbOpRepo.search(
                SearchArbOpRepoRequest.SearchCriteria(ArbitrageOpportunityFilter.DEFAULT)
            )

            val existingActive = when (searchResult) {
                is ArbOpRepoResponse.Multiple -> {
                    // Фильтруем только активные (где endTimestamp == null)
                    searchResult.arbOps.filter { it.endTimestamp == null }
                }

                is ArbOpRepoResponse.Error -> {
                    logger.error("Ошибка получения существующих возможностей: ${searchResult.errors}")
                    errors.addAll(searchResult.errors)
                    return@doWithLogging
                }

                is ArbOpRepoResponse.Single -> error("Неожиданный вариант для search")
            }

            logger.info("Найдено ${existingActive.size} активных возможностей в БД")

            val existingMap = existingActive.associateBy { it.fastKey }

            val newMap = arbOps.associateBy { it.fastKey }

            // 3. Определяем операции
            val toCreate = mutableListOf<CexToCexArbitrageOpportunity>()
            val toUpdate = mutableListOf<CexToCexArbitrageOpportunity>()
            val toClose = mutableListOf<CexToCexArbitrageOpportunity>()

            // Новые возможности и обновления существующих
            newMap.forEach { (key, newOp) ->
                val existing = existingMap[key]
                if (existing != null) {
                    // Обновляем существующую: сохраняем id и startTimestamp, обновляем остальные поля
                    toUpdate.add(
                        newOp.copy(
                            id = existing.id,
                            startTimestamp = existing.startTimestamp
                        )
                    )
                } else {
                    // Создаем новую
                    toCreate.add(newOp)
                }
            }

            // Закрываем возможности, которых нет в новых данных
            val currentTimestamp = Timestamp(Clock.System.now().epochSeconds)
            existingMap.forEach { (key, existingOp) ->
                if (!newMap.containsKey(key)) {
                    // Закрываем: устанавливаем endTimestamp
                    toClose.add(existingOp.copy(endTimestamp = currentTimestamp))
                }
            }

            logger.info("Создать: ${toCreate.size}, обновить: ${toUpdate.size}, закрыть: ${toClose.size}")

            // 4. Выполняем операции с обработкой результатов

            // Создание новых
            if (toCreate.isNotEmpty()) {
                when (val createResult = arbOpRepo.create(CreateArbOpRepoRequest.Items(toCreate))) {
                    is ArbOpRepoResponse.Multiple -> {
                        logger.info("Успешно создано ${createResult.arbOps.size} возможностей")
                    }

                    is ArbOpRepoResponse.Single -> Unit
                    is ArbOpRepoResponse.Error -> {
                        logger.error("Ошибка создания возможностей: ${createResult.errors}")
                        errors.addAll(createResult.errors)
                    }
                }
            }

            // Обновление существующих
            if (toUpdate.isNotEmpty()) {
                when (val updateResult = arbOpRepo.update(UpdateArbOpRepoRequest.Items(toUpdate))) {
                    is ArbOpRepoResponse.Multiple -> {
                        logger.info("Успешно обновлено ${updateResult.arbOps.size} возможностей")
                    }

                    is ArbOpRepoResponse.Single -> Unit
                    is ArbOpRepoResponse.Error -> {
                        logger.error("Ошибка обновления возможностей: ${updateResult.errors}")
                        errors.addAll(updateResult.errors)
                    }
                }
            }

            // Закрытие неактуальных
            if (toClose.isNotEmpty()) {
                when (val closeResult = arbOpRepo.update(UpdateArbOpRepoRequest.Items(toClose))) {
                    is ArbOpRepoResponse.Multiple -> {
                        logger.info("Успешно закрыто ${closeResult.arbOps.size} возможностей")
                    }

                    is ArbOpRepoResponse.Single -> Unit
                    is ArbOpRepoResponse.Error -> {
                        logger.error("Ошибка закрытия возможностей: ${closeResult.errors}")
                        errors.addAll(closeResult.errors)
                    }
                }
            }
        }
    }
}