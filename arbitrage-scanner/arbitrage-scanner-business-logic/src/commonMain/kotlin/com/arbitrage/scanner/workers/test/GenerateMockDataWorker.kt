package com.arbitrage.scanner.workers.test

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::generateMockDataWorker

/**
 * Worker для генерации mock данных в TEST режиме
 *
 * Генерирует тестовые данные о ценах CEX токенов на биржах
 * и сохраняет их в CexPriceRepository для последующей обработки
 *
 * ## Предусловия:
 * - state == State.RUNNING
 * - command == Command.RECALCULATE
 * - workMode == WorkMode.TEST
 *
 * ## Постусловия:
 * - CexPriceRepository содержит сгенерированные mock данные
 * - state остаётся State.RUNNING для продолжения цепочки
 *
 * @param title Название worker'а для логирования и отладки
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.generateMockDataWorker(
    title: String
) = worker {
    this.title = title
    description = "Генерация тестовых данных CEX цен для режима TEST"

    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)

    handle {
        val requestId = requestId.toString()

        logger.doWithLogging(id = requestId, level = LogLevel.INFO) {
            // Конфигурация генерации mock данных
            val tokenCount = 50
            val exchangeCount = 5
            val seed = 42
            val priceVariation = 0.05 // 5% вариация цен

            logger.info(
                msg = "Генерация mock данных",
                marker = "GENERATE_MOCK_DATA",
                data = mapOf(
                    "requestId" to requestId,
                    "tokenCount" to tokenCount,
                    "exchangeCount" to exchangeCount,
                    "seed" to seed,
                    "priceVariation" to priceVariation
                )
            )

            // Генерация тестовых цен
            val mockPrices = StubsDataFactory.generateCexPrices(
                tokenCount = tokenCount,
                exchangeCount = exchangeCount,
                seed = seed,
                priceVariation = priceVariation
            )

            logger.info(
                msg = "Тестовые данные сгенерированы",
                marker = "MOCK_DATA_GENERATED",
                data = mapOf(
                    "requestId" to requestId,
                    "generatedPricesCount" to mockPrices.size
                )
            )

            // Сохранение в репозиторий
            config.cexPriceRepository.saveAll(mockPrices)

            logger.info(
                msg = "Mock данные сохранены в репозиторий",
                marker = "MOCK_DATA_SAVED",
                data = mapOf(
                    "requestId" to requestId,
                    "savedPricesCount" to mockPrices.size
                )
            )
        }
    }
}
