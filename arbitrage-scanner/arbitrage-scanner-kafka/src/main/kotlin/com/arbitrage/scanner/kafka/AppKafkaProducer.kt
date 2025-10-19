package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.kafka.config.KafkaConfig
import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.Future

/**
 * Класс для работы с Kafka Producer.
 * Инкапсулирует подключение к Kafka и отправку сообщений.
 *
 * @property config конфигурация Kafka
 * @property loggerProvider провайдер логгера для системы логирования
 * @property defaultTopic топик по умолчанию для отправки сообщений (из config.outTopic)
 */
class AppKafkaProducer(
    private val config: KafkaConfig,
    loggerProvider: ArbScanLoggerProvider,
    private val defaultTopic: String = config.outTopic
) : AutoCloseable {

    private val logger: ArbScanLogWrapper = loggerProvider.logger(AppKafkaProducer::class)
    private val producer: KafkaProducer<String, String> = config.createProducer()

    init {
        logger.info(
            "Инициализация Kafka Producer с конфигурацией: ${config.bootstrapServers}, топик по умолчанию: $defaultTopic"
        )
    }

    /**
     * Отправка сообщения в топик по умолчанию.
     *
     * @param message сообщение для отправки
     * @param key ключ сообщения (опционально)
     * @return Future с метаданными отправленного сообщения
     */
    fun send(
        message: String,
        key: String? = null
    ): Future<RecordMetadata> {
        return send(defaultTopic, message, key)
    }

    /**
     * Отправка сообщения в указанный топик.
     *
     * @param topic топик для отправки
     * @param message сообщение для отправки
     * @param key ключ сообщения (опционально)
     * @return Future с метаданными отправленного сообщения
     */
    fun send(
        topic: String,
        message: String,
        key: String? = null
    ): Future<RecordMetadata> {
        return try {
            logger.debug("Отправка сообщения в топик '$topic'${key?.let { ", ключ: $it" } ?: ""}")

            val record = ProducerRecord(topic, key, message)
            val future = producer.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error(
                        msg = "Ошибка при отправке сообщения в топик '$topic'",
                        e = exception
                    )
                } else {
                    logger.debug(
                        "Сообщение отправлено успешно: топик=${metadata.topic()}, " +
                        "партиция=${metadata.partition()}, " +
                        "offset=${metadata.offset()}"
                    )
                }
            }

            future
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при отправке сообщения в Kafka", e = e)
            throw e
        }
    }

    /**
     * Синхронная отправка сообщения (ожидание подтверждения).
     *
     * @param message сообщение для отправки
     * @param key ключ сообщения (опционально)
     * @return RecordMetadata с информацией о отправленном сообщении
     */
    fun sendSync(
        message: String,
        key: String? = null
    ): RecordMetadata {
        return sendSync(defaultTopic, message, key)
    }

    /**
     * Синхронная отправка сообщения в указанный топик (ожидание подтверждения).
     *
     * @param topic топик для отправки
     * @param message сообщение для отправки
     * @param key ключ сообщения (опционально)
     * @return RecordMetadata с информацией о отправленном сообщении
     */
    fun sendSync(
        topic: String,
        message: String,
        key: String? = null
    ): RecordMetadata {
        return try {
            logger.debug("Синхронная отправка сообщения в топик '$topic'${key?.let { ", ключ: $it" } ?: ""}")

            val record = ProducerRecord(topic, key, message)
            val metadata = producer.send(record).get()

            logger.debug(
                "Сообщение отправлено синхронно: топик=${metadata.topic()}, " +
                "партиция=${metadata.partition()}, " +
                "offset=${metadata.offset()}"
            )

            metadata
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при синхронной отправке сообщения в Kafka", e = e)
            throw e
        }
    }

    /**
     * Принудительная отправка всех буферизованных сообщений.
     */
    fun flush() {
        try {
            logger.debug("Flush буферизованных сообщений")
            producer.flush()
            logger.debug("Flush завершен")
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при flush Kafka Producer", e = e)
            throw e
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
