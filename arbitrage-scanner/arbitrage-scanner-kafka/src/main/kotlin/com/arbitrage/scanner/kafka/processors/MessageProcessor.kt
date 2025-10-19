package com.arbitrage.scanner.kafka.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.fromRequestJsonString
import com.arbitrage.scanner.kafka.AppKafkaProducer
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.mappers.toTransport
import com.arbitrage.scanner.processors.processContext
import com.arbitrage.scanner.toResponseJsonString
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord
import kotlin.reflect.KFunction

/**
 * Обработка сообщения из Kafka с использованием бизнес-логики.
 * Аналог processRequest для Ktor, но для Kafka сообщений.
 *
 * @param record запись из Kafka Consumer
 * @param producer продьюсер для отправки ответа
 * @param businessLogicProcessor процессор бизнес-логики
 * @param loggerProvider провайдер логгера
 * @param kFun функция для логирования
 * @param logId идентификатор лога
 * @param json экземпляр Json для сериализации/десериализации (по умолчанию игнорирует неизвестные ключи)
 */
suspend inline fun processMessage(
    record: ConsumerRecord<String, String>,
    producer: AppKafkaProducer,
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
    kFun: KFunction<*>,
    logId: String,
    json: Json = Json { ignoreUnknownKeys = true }
) {
    processContext(
        prepareContextFromRequest = {
            val request = json.fromRequestJsonString<IRequest>(record.value())
            fromTransport(request = request)
        },
        resolveContextToResponse = {
            val response = toTransport()
            val responseJson = json.toResponseJsonString(response)
            producer.send(responseJson)
        },
        executeLogic = { businessLogicProcessor.exec(this) },
        loggerProvider = loggerProvider,
        kFun = kFun,
        logId = logId,
    )
}
