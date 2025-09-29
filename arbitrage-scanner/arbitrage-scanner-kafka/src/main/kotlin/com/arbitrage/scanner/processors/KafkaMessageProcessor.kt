package com.arbitrage.scanner.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.fromRequestJsonString
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.mappers.toTransport
import com.arbitrage.scanner.models.KafkaMessage
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Центральный процессор для обработки Kafka сообщений
 *
 * Координирует:
 * - Десериализацию запроса из JSON
 * - Создание и подготовку контекста
 * - Вызов бизнес-логики
 * - Сериализацию и отправку ответа
 *
 * @property json экземпляр Json для сериализации/десериализации
 * @property businessLogicProcessor процессор бизнес-логики
 * @property responseProducer producer для отправки ответов
 * @property loggerProvider провайдер логгера
 */
class KafkaMessageProcessor(
    private val json: Json,
    private val businessLogicProcessor: BusinessLogicProcessor,
    private val responseProducer: ResponseProducer,
    private val loggerProvider: ArbScanLoggerProvider
) {
    private val logger = loggerProvider.logger("KafkaMessageProcessor")

    /**
     * Обрабатывает сообщение из Kafka
     *
     * @param message Kafka сообщение с метаданными и payload
     */
    @OptIn(ExperimentalTime::class)
    suspend fun processMessage(message: KafkaMessage) {
        try {
            logger.info(
                msg = "Processing Kafka message",
                marker = "KAFKA",
                data = "correlationId=${message.correlationId}, type=${message.requestType}"
            )

            // Десериализация запроса из JSON
            val request = json.fromRequestJsonString<com.arbitrage.scanner.api.v1.models.IRequest>(message.payload)

            // Создание и подготовка контекста
            val context = Context(
                requestId = com.arbitrage.scanner.base.RequestId(message.correlationId),
                startTimestamp = Timestamp(value = Clock.System.now().epochSeconds)
            )

            // Маппинг из transport модели в доменную
            context.fromTransport(request)

            // Обработка через бизнес-логику
            processKafkaContext(
                context = context,
                businessLogicProcessor = businessLogicProcessor,
                loggerProvider = loggerProvider
            )

            // Маппинг из доменной модели в transport
            val response = context.toTransport()

            // Отправка ответа
            responseProducer.sendResponse(
                correlationId = message.correlationId,
                response = response
            )

            logger.info(
                msg = "Kafka message processed successfully",
                marker = "KAFKA",
                data = "correlationId=${message.correlationId}"
            )

        } catch (e: Exception) {
            logger.error(
                msg = "Failed to process Kafka message",
                marker = "KAFKA",
                data = "correlationId=${message.correlationId}",
                e = e
            )
            // В production можно отправить error response
            throw e
        }
    }
}