package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.toResponseJsonString
import com.arbitrage.scanner.config.ProducerConfig
import com.arbitrage.scanner.config.KafkaConfig
import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig as ApacheProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

/**
 * Реализация ResponseProducer для отправки сообщений в Kafka
 *
 * Инкапсулирует логику:
 * - Сериализации IResponse в JSON
 * - Формирования Kafka записей с заголовками
 * - Обработки ошибок отправки
 * - Управления ресурсами producer
 *
 * @property config конфигурация producer
 * @property kafkaConfig общая конфигурация Kafka
 * @property json экземпляр Json для сериализации
 * @property logger логгер для записи операций
 */
class KafkaResponseProducer(
    private val config: ProducerConfig,
    private val kafkaConfig: KafkaConfig,
    private val json: Json,
    private val logger: ArbScanLogWrapper
) : ResponseProducer {

    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply {
            put(ApacheProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
            put(ApacheProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ApacheProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ApacheProducerConfig.CLIENT_ID_CONFIG, config.clientId)
            put(ApacheProducerConfig.ACKS_CONFIG, config.acks)
            put(ApacheProducerConfig.RETRIES_CONFIG, config.retries)
            put(ApacheProducerConfig.BATCH_SIZE_CONFIG, config.batchSize)
            put(ApacheProducerConfig.LINGER_MS_CONFIG, config.lingerMs)
            put(ApacheProducerConfig.COMPRESSION_TYPE_CONFIG, config.compressionType)
            put(ApacheProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, config.maxInFlightRequestsPerConnection)

            // Добавляем дополнительные свойства из конфигурации
            config.properties.forEach { (key, value) ->
                put(key, value)
            }
        }
        producer = KafkaProducer(props)

        logger.info(
            msg = "Kafka producer initialized",
            marker = "KAFKA_PRODUCER",
            data = "clientId=${config.clientId}, topic=${config.topics.responses}"
        )
    }

    override suspend fun sendResponse(
        correlationId: String,
        response: IResponse
    ) = withContext(Dispatchers.IO) {
        try {
            // Сериализуем ответ в JSON
            val jsonValue = json.toResponseJsonString(response)

            // Создаем Kafka запись с key (correlation ID) и заголовками
            val record = ProducerRecord<String, String>(
                config.topics.responses,
                correlationId, // key для партиционирования по correlation ID
                jsonValue
            ).apply {
                // Добавляем заголовки для метаданных
                headers().add("correlation-id", correlationId.toByteArray())
                headers().add("response-type", response::class.simpleName?.toByteArray() ?: "Unknown".toByteArray())
                headers().add("timestamp", System.currentTimeMillis().toString().toByteArray())
            }

            // Синхронная отправка для гарантии доставки
            producer.send(record).get()

            logger.info(
                msg = "Response sent successfully",
                marker = "KAFKA_PRODUCER",
                data = "correlationId=$correlationId, type=${response::class.simpleName}"
            )
        } catch (e: Exception) {
            logger.error(
                msg = "Failed to send response",
                marker = "KAFKA_PRODUCER",
                data = "correlationId=$correlationId",
                e = e
            )
            throw e
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        try {
            producer.close()
            logger.info(
                msg = "Kafka producer closed successfully",
                marker = "KAFKA_PRODUCER"
            )
        } catch (e: Exception) {
            logger.error(
                msg = "Error closing Kafka producer",
                marker = "KAFKA_PRODUCER",
                e = e
            )
            throw e
        }
    }
}