package com.arbitrage.scanner.app.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlinx.coroutines.CancellationException
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Класс для работы с Kafka Producer.
 * Инкапсулирует подключение к Kafka и отправку сообщений.
 *
 * @property producer экземпляр Producer для подключения к Kafka
 * @property loggerProvider провайдер логгера для системы логирования
 * @property defaultTopic топик по умолчанию для отправки сообщений
 */
class AppKafkaProducer(
    private val producer: Producer<String, String>,
    loggerProvider: ArbScanLoggerProvider,
    private val defaultTopic: String
) : AutoCloseable {

    private val logger: ArbScanLogWrapper = loggerProvider.logger(AppKafkaProducer::class)

    init {
        logger.info(
            "Инициализация Kafka Producer, топик по умолчанию: $defaultTopic"
        )
    }

    /**
     * Отправка сообщения в топик по умолчанию.
     *
     * @param message сообщение для отправки
     * @return RecordMetadata с информацией о отправленном сообщении
     */
    suspend fun send(message: String): RecordMetadata = suspendCoroutine { continuation ->
        try {
            logger.debug("Отправка сообщения в топик '$defaultTopic'")

            val record = ProducerRecord<String, String>(defaultTopic, null, message)
            producer.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error(
                        msg = "Ошибка при отправке сообщения в топик '$defaultTopic'",
                        e = exception
                    )
                    continuation.resumeWithException(exception)
                } else {
                    logger.debug(
                        "Сообщение отправлено успешно: топик=${metadata.topic()}, " +
                                "партиция=${metadata.partition()}, " +
                                "offset=${metadata.offset()}"
                    )
                    continuation.resume(metadata)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при отправке сообщения в Kafka", e = e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Закрытие соединения с Kafka.
     */
    override fun close() {
        try {
            logger.info("Закрытие Kafka Producer")
            producer.close()
            logger.info("Kafka Producer закрыт")
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при закрытии Kafka Producer", e = e)
        }
    }
}
