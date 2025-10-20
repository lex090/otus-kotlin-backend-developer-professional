package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Класс для работы с Kafka Consumer.
 * Инкапсулирует подключение к Kafka и управление считыванием сообщений.
 *
 * @property consumer экземпляр Consumer для подключения к Kafka
 * @property loggerProvider провайдер логгера для системы логирования
 * @property topics список топиков для подписки
 * @property pollTimeout таймаут опроса Kafka (по умолчанию 1 секунда)
 */
class AppKafkaConsumer(
    private val consumer: Consumer<String, String>,
    loggerProvider: ArbScanLoggerProvider,
    private val topics: List<String>,
    private val pollTimeout: Duration = Duration.ofSeconds(1)
) : AutoCloseable {

    private val logger: ArbScanLogWrapper = loggerProvider.logger(AppKafkaConsumer::class)
    private val isRunning = AtomicBoolean(false)

    init {
        logger.info(
            "Инициализация Kafka Consumer для топиков: $topics"
        )
    }

    /**
     * Подписка на топик и начало считывания сообщений.
     *
     * @param messageHandler функция-обработчик для каждого полученного сообщения
     */
    suspend fun subscribe(
        messageHandler: suspend (ConsumerRecord<String, String>) -> Unit
    ) {
        try {
            consumer.subscribe(topics)
            logger.info("Подписка на топики: $topics")

            isRunning.set(true)

            while (isRunning.get()) {
                val records = consumer.poll(pollTimeout)

                if (records.isEmpty) {
                    logger.debug("Нет новых сообщений в топиках: $topics")
                } else {
                    logger.debug("Получено ${records.count()} сообщений")

                    records.forEach { record ->
                        try {
                            logger.debug(
                                "Обработка сообщения: топик=${record.topic()}, " +
                                "партиция=${record.partition()}, " +
                                "offset=${record.offset()}, " +
                                "ключ=${record.key()}"
                            )
                            messageHandler(record)
                        } catch (e: Exception) {
                            logger.error(msg = "Ошибка при обработке сообщения из топика ${record.topic()}", e = e)
                        }
                    }

                    // Подтверждение обработки сообщений
                    consumer.commitSync()
                    logger.debug("Обработано и закоммичено ${records.count()} сообщений")
                }
            }
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при работе с Kafka Consumer", e = e)
            throw e
        } finally {
            logger.info("Остановка Kafka Consumer")
            close()
        }
    }

    /**
     * Остановка считывания сообщений.
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Остановка Kafka Consumer")
        }
    }

    /**
     * Закрытие соединения с Kafka.
     */
    override fun close() {
        stop()
        try {
            consumer.close()
            logger.info("Kafka Consumer закрыт")
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при закрытии Kafka Consumer", e = e)
        }
    }
}
