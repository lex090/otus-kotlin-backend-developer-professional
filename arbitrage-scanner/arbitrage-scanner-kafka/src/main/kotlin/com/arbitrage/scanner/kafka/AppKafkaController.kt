package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.kafka.processors.processMessage
import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord

/**
 * Контроллер для обработки сообщений из Kafka.
 * Связывает AppKafkaConsumer и AppKafkaProducer, обрабатывая сообщения через бизнес-логику.
 *
 * @property consumer экземпляр AppKafkaConsumer для считывания сообщений
 * @property producer экземпляр AppKafkaProducer для отправки сообщений
 * @property businessLogicProcessor процессор бизнес-логики
 * @property loggerProvider провайдер логгера для системы логирования
 * @property json экземпляр Json для сериализации/десериализации
 */
class AppKafkaController(
    private val consumer: AppKafkaConsumer,
    private val producer: AppKafkaProducer,
    private val businessLogicProcessor: BusinessLogicProcessor,
    private val loggerProvider: ArbScanLoggerProvider,
    private val json: Json
) : AutoCloseable {

    private val logger: ArbScanLogWrapper = loggerProvider.logger(AppKafkaController::class)

    init {
        logger.info("Инициализация AppKafkaController")
    }

    /**
     * Запуск контроллера для обработки сообщений из Kafka.
     * Подписывается на топики и начинает обрабатывать сообщения из Flow.
     */
    suspend fun start() {
        logger.info("Запуск AppKafkaController")

        consumer.subscribe()
            .onEach { record ->
                handleMessage(record)
            }
            .collect()
    }

    /**
     * Обработка одного сообщения из Kafka.
     * Определяет тип сообщения через полиморфную десериализацию и вызывает соответствующий обработчик.
     *
     * @param record запись из Kafka Consumer
     */
    private suspend fun handleMessage(record: ConsumerRecord<String, String>) {
        try {
            logger.debug("Обработка сообщения из топика ${record.topic()}")
            processMessage(
                record = record,
                producer = producer,
                businessLogicProcessor = businessLogicProcessor,
                loggerProvider = loggerProvider,
                kFun = ::handleMessage,
                logId = "kafka-read",
                json = json
            )
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при обработке сообщения", e = e)
        }
    }

    /**
     * Закрытие контроллера и всех связанных ресурсов.
     */
    override fun close() {
        logger.info("Закрытие AppKafkaController")
        consumer.close()
        producer.close()
    }
}
