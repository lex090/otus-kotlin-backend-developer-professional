package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

/**
 * Класс для работы с Kafka Consumer на основе Flow API.
 * Инкапсулирует подключение к Kafka и управление считыванием сообщений.
 *
 * @property consumer экземпляр Consumer для подключения к Kafka
 * @property loggerProvider провайдер логгера для системы логирования
 * @property topics список топиков для подписки
 * @property pollTimeout таймаут опроса Kafka (по умолчанию 1 секунда)
 * @property dispatcher диспетчер для выполнения blocking операций (по умолчанию Dispatchers.IO)
 */
class AppKafkaConsumer(
    private val consumer: Consumer<String, String>,
    loggerProvider: ArbScanLoggerProvider,
    private val topics: List<String>,
    private val pollTimeout: Duration = Duration.ofSeconds(1),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AutoCloseable {

    private val logger: ArbScanLogWrapper = loggerProvider.logger(AppKafkaConsumer::class)

    init {
        logger.info("Инициализация Kafka Consumer для топиков: $topics")
    }

    /**
     * Подписка на топики и создание Flow сообщений из Kafka.
     *
     * Flow автоматически останавливается при отмене корутины.
     * Blocking вызовы выполняются в Dispatchers.IO.
     *
     * @return Flow сообщений из Kafka
     */
    fun subscribe(): Flow<ConsumerRecord<String, String>> = flow {
        try {
            consumer.subscribe(topics)
            logger.info("Подписка на топики: $topics")

            while (currentCoroutineContext().isActive) {
                // Проверка отмены перед blocking вызовом
                currentCoroutineContext().ensureActive()

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

                            // Проверка отмены перед эмиссией
                            currentCoroutineContext().ensureActive()

                            emit(record)
                        } catch (e: Exception) {
                            logger.error(
                                msg = "Ошибка при обработке сообщения из топика ${record.topic()}",
                                e = e
                            )
                        }
                    }

                    // Подтверждение обработки сообщений
                    consumer.commitSync()
                    logger.debug("Обработано и закоммичено ${records.count()} сообщений")
                }
                delay(10) // Небольшая задержка перед следующим опросом
            }
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при работе с Kafka Consumer", e = e)
            throw e
        } finally {
            logger.info("Остановка Kafka Consumer")
        }
    }.flowOn(dispatcher) // Blocking calls выполняются в заданном dispatcher

    /**
     * Закрытие соединения с Kafka.
     */
    override fun close() {
        try {
            consumer.close()
            logger.info("Kafka Consumer закрыт")
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при закрытии Kafka Consumer", e = e)
        }
    }
}
