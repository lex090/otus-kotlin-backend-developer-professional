package com.arbitrage.scanner.kafka.example

import com.arbitrage.scanner.kafka.AppKafkaConsumer
import com.arbitrage.scanner.kafka.AppKafkaProducer
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

/**
 * Пример использования AppKafkaProducer.
 *
 * Этот класс демонстрирует:
 * 1. Загрузку конфигурации из application.yaml
 * 2. Создание экземпляра AppKafkaProducer
 * 3. Отправку сообщений в Kafka
 */
object ProducerExample {

    private val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)
    private val logger = loggerProvider.logger(ProducerExample::class)

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Запуск примера Kafka Producer")

        // 1. Загрузка конфигурации
        val config = KafkaConfigLoader.load()
        logger.info("Загружена конфигурация: ${config.bootstrapServers}")

        // 2. Создание producer
        val producer = AppKafkaProducer(config, loggerProvider)

        try {
            // 3. Асинхронная отправка сообщений
            logger.info("Отправка сообщений асинхронно...")
            for (i in 1..10) {
                val message = "Сообщение №$i: ${System.currentTimeMillis()}"
                producer.send(message, key = "key-$i")
                logger.info("Отправлено: $message")
                Thread.sleep(100)
            }

            // Принудительная отправка буферизованных сообщений
            producer.flush()
            logger.info("Все сообщения отправлены")

            // 4. Синхронная отправка сообщения
            logger.info("Отправка сообщения синхронно...")
            val metadata = producer.sendSync("Синхронное сообщение", key = "sync-key")
            logger.info(
                "Синхронное сообщение отправлено успешно: " +
                "топик=${metadata.topic()}, партиция=${metadata.partition()}, offset=${metadata.offset()}"
            )

        } finally {
            producer.close()
            logger.info("Producer закрыт")
        }
    }
}

/**
 * Пример совместного использования Producer и Consumer.
 */
object ProducerConsumerExample {

    private val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)
    private val logger = loggerProvider.logger(ProducerConsumerExample::class)

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Запуск примера Producer + Consumer")

        val config = KafkaConfigLoader.load()

        // Создаем producer и consumer
        val producer = AppKafkaProducer(config, loggerProvider)
        val consumer = AppKafkaConsumer(config, loggerProvider)

        // Запускаем consumer в отдельном потоке
        val consumerThread = Thread {
            consumer.subscribe { record ->
                logger.info("Consumer получил: ${record.value()}")
            }
        }.apply {
            isDaemon = true
            start()
        }

        // Даем время consumer'у подключиться
        Thread.sleep(2000)

        try {
            // Отправляем несколько сообщений
            logger.info("Producer отправляет сообщения...")
            for (i in 1..5) {
                val message = "Тестовое сообщение $i"
                producer.send(message)
                logger.info("Producer отправил: $message")
                Thread.sleep(1000)
            }

            producer.flush()

            // Даем время consumer'у обработать сообщения
            Thread.sleep(5000)

        } finally {
            consumer.stop()
            producer.close()
            consumerThread.join(2000)
            logger.info("Пример завершен")
        }
    }
}

