package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexExchangeIds
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexTokenIdsFilter
import com.arbitrage.scanner.workers.commandProcessor
import com.arbitrage.scanner.workers.initStatus
import com.arbitrage.scanner.workers.read.prepareReadResponseWorker
import com.arbitrage.scanner.workers.read.readArbOpWorker
import com.arbitrage.scanner.workers.recalculate.analyzeArbOpChangesWorker
import com.arbitrage.scanner.workers.recalculate.closeInactiveArbOpsWorker
import com.arbitrage.scanner.workers.recalculate.createNewArbOpsWorker
import com.arbitrage.scanner.workers.recalculate.findArbOpsWorker
import com.arbitrage.scanner.workers.recalculate.getCexPricesWorker
import com.arbitrage.scanner.workers.recalculate.loadActiveArbOpsWorker
import com.arbitrage.scanner.workers.recalculate.prepareRecalculateResponseWorker
import com.arbitrage.scanner.workers.recalculate.updateExistingArbOpsWorker
import com.arbitrage.scanner.workers.search.prepareSearchResponseWorker
import com.arbitrage.scanner.workers.search.searchArbOpWorker
import com.arbitrage.scanner.workers.setupArbOpRepoWorker
import com.arbitrage.scanner.workers.setupCexPriceClientServiceWorker
import com.arbitrage.scanner.workers.stubs.noStubCaseWorker
import com.arbitrage.scanner.workers.stubs.readBadIdStubWorker
import com.arbitrage.scanner.workers.stubs.readNotFoundStubWorker
import com.arbitrage.scanner.workers.stubs.readSuccessStubWorker
import com.arbitrage.scanner.workers.stubs.recalculateSuccessStubWorker
import com.arbitrage.scanner.workers.stubs.searchNotFoundStubWorker
import com.arbitrage.scanner.workers.stubs.searchSuccessStubWorker
import com.arbitrage.scanner.workers.validation.validateCexTokenIdsWorker
import com.arbitrage.scanner.workers.validation.validateExchangeIdsWorker
import com.arbitrage.scanner.workers.validation.validateIdFormatWorker
import com.arbitrage.scanner.workers.validation.validateIdMaxLengthWorker
import com.arbitrage.scanner.workers.validation.validateIdMinLengthWorker
import com.arbitrage.scanner.workers.validation.validateIdNotEmptyWorker
import com.arbitrage.scanner.workers.validation.validateSpreadRangeWorker
import com.arbitrage.scanner.workers.validationProcessor
import com.arbitrage.scanner.workers.workModProcessor
import com.crowdproj.kotlin.cor.handlers.chain
import com.crowdproj.kotlin.cor.handlers.worker
import com.crowdproj.kotlin.cor.rootChain

class BusinessLogicProcessorImpl(
    deps: BusinessLogicProcessorImplDeps,
) : BusinessLogicProcessor {

    override suspend fun exec(ctx: Context) = corChain.exec(context = ctx)

    private val corChain = rootChain(config = deps) {
        initStatus("Инициализация статуса обработки запроса")
        setupCexPriceClientServiceWorker("Установка сервиса получения цен с CEX криптобирж")
        setupArbOpRepoWorker("Установка репозитория арбитражных ситуаций")

        commandProcessor(title = "Обработка события recalculate", command = Command.RECALCULATE) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                recalculateSuccessStubWorker(title = "Обработка стаба SUCCESS")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }

            chain {
                title = "Основная логика обработки recalculate"
                getCexPricesWorker("Получение цен с CEX бирж")
                findArbOpsWorker("Поиск новых арбитражных возможностей")
                loadActiveArbOpsWorker("Загрузка активных возможностей из БД")
                analyzeArbOpChangesWorker("Анализ изменений (создать/обновить/закрыть)")
                createNewArbOpsWorker("Создание новых возможностей")
                updateExistingArbOpsWorker("Обновление существующих возможностей")
                closeInactiveArbOpsWorker("Закрытие неактуальных возможностей")
                prepareRecalculateResponseWorker("Подготовка ответа")
            }
        }

        commandProcessor(title = "Обработка события read", command = Command.READ) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                readSuccessStubWorker(title = "Обработка стаба SUCCESS")
                readNotFoundStubWorker(title = "Обработка стаба NOT_FOUND")
                readBadIdStubWorker(title = "Обработка стаба BAD_ID")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }

            validationProcessor {
                worker("Копируем поля в arbitrageOpportunityReadRequestValidating") {
                    arbitrageOpportunityReadRequestValidating = arbitrageOpportunityReadRequest
                }
                worker("Нормализация ID - удаление лишних пробелов") {
                    arbitrageOpportunityReadRequestValidating = ArbitrageOpportunityId(
                        arbitrageOpportunityReadRequestValidating.value.trim()
                    )
                }

                // Последовательность валидации
                validateIdNotEmptyWorker("Проверка на пустой ID")
                validateIdMinLengthWorker("Проверка минимальной длины ID")
                validateIdMaxLengthWorker("Проверка максимальной длины ID")
                validateIdFormatWorker("Проверка формата ID")

                worker("Финализация валидированных данных") {
                    arbitrageOpportunityReadRequestValidated = arbitrageOpportunityReadRequestValidating
                }

                chain {
                    title = "Основная логика обработки read"
                    readArbOpWorker("Чтение арбитражной возможности из БД")
                    prepareReadResponseWorker("Подготовка ответа")
                }
            }
        }

        commandProcessor(title = "Обработка события search", command = Command.SEARCH) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                searchSuccessStubWorker(title = "Обработка стаба SUCCESS")
                searchNotFoundStubWorker(title = "Обработка стаба NOT_FOUND")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }

            validationProcessor {
                worker("Копируем поля в arbitrageOpportunitySearchRequestValidating") {
                    arbitrageOpportunitySearchRequestValidating = arbitrageOpportunitySearchRequest
                }

                worker("Нормализация ID в фильтрах") {
                    // Нормализуем все ID - удаляем лишние пробелы
                    val normalizedFilter = arbitrageOpportunitySearchRequestValidating.copy(
                        cexTokenIdsFilter = CexTokenIdsFilter(
                            arbitrageOpportunitySearchRequestValidating.cexTokenIdsFilter.value
                                .map { CexTokenId(it.value.trim()) }.toSet()
                        ),
                        buyExchangeIds = CexExchangeIds(
                            arbitrageOpportunitySearchRequestValidating.buyExchangeIds.value
                                .map { CexExchangeId(it.value.trim()) }.toSet()
                        ),
                        sellExchangeIds = CexExchangeIds(
                            arbitrageOpportunitySearchRequestValidating.sellExchangeIds.value
                                .map { CexExchangeId(it.value.trim()) }.toSet()
                        ),
                    )
                    arbitrageOpportunitySearchRequestValidating = normalizedFilter
                }

                // Последовательность валидации фильтров
                validateCexTokenIdsWorker("Проверка ID CEX токенов")
                validateExchangeIdsWorker("Проверка ID бирж покупки и продажи")
                validateSpreadRangeWorker("Проверка диапазона спредов")

                worker("Финализация валидированных данных") {
                    arbitrageOpportunitySearchRequestValidated = arbitrageOpportunitySearchRequestValidating
                }

                chain {
                    title = "Основная логика обработки search"
                    searchArbOpWorker("Поиск арбитражных возможностей в БД")
                    prepareSearchResponseWorker("Подготовка ответа")
                }
            }
        }
    }.build()
}