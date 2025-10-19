package com.arbitrage.scanner.kafka.example

import com.arbitrage.scanner.kafka.AppKafkaConsumer
import com.arbitrage.scanner.kafka.config.KafkaConfigLoader
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback

/**
 * Пример использования AppKafkaConsumer.
 *
 * Этот класс демонстрирует:
 * 1. Загрузку конфигурации из application.yaml
 * 2. Создание экземпляра AppKafkaConsumer
 * 3. Подписку на топики и обработку сообщений
 * 4. Корректное завершение работы
 */
object ConsumerExample {

    private val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)
    private val logger = loggerProvider.logger(ConsumerExample::class)

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Запуск примера Kafka Consumer")

        // 1. Загрузка конфигурации
        val config = KafkaConfigLoader.load()
        logger.info("Загружена конфигурация: ${config.bootstrapServers}")

        // 2. Создание consumer
        val consumer = AppKafkaConsumer(config, loggerProvider)

        // 3. Обработка Ctrl+C для корректного завершения
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Получен сигнал завершения, останавливаем consumer")
            consumer.close()
        })

        try {
            // 4. Синхронная подписка на топик с обработкой сообщений
            consumer.subscribe { record ->
                logger.info(
                    """
                    |Получено сообщение:
                    |  Топик: ${record.topic()}
                    |  Партиция: ${record.partition()}
                    |  Offset: ${record.offset()}
                    |  Ключ: ${record.key()}
                    |  Значение: ${record.value()}
                    |  Timestamp: ${record.timestamp()}
                    """.trimMargin()
                )

                // Здесь можно добавить свою логику обработки
                processMessage(record.value())
            }
        } catch (e: Exception) {
            logger.error(msg = "Ошибка при работе consumer", e = e)
        } finally {
            consumer.close()
            logger.info("Consumer закрыт")
        }
    }

    /**
     * Пример обработки сообщения.
     */
    private fun processMessage(message: String) {
        // Здесь может быть ваша логика:
        // - десериализация JSON
        // - валидация данных
        // - сохранение в БД
        // - отправка в другие системы
        logger.debug("Обработка сообщения: $message")
    }
}

