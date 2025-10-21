package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit тесты для AppKafkaController с использованием MockConsumer и MockProducer
 */
class AppKafkaControllerTest {

    private val testInTopic = "test-in-topic"
    private val testOutTopic = "test-out-topic"
    private val testPartition = 0

    /**
     * Создает настроенный MockConsumer с начальными параметрами
     */
    private fun createMockConsumer(): MockConsumer<String, String> {
        return MockConsumer<String, String>(OffsetResetStrategy.EARLIEST).apply {
            val topicPartition = TopicPartition(testInTopic, testPartition)
            subscribe(listOf(testInTopic))
            rebalance(listOf(topicPartition))
            updateBeginningOffsets(mapOf(topicPartition to 0L))
            updateEndOffsets(mapOf(topicPartition to 0L))
        }
    }

    /**
     * Создает настроенный MockProducer с автоматическим подтверждением
     */
    private fun createMockProducer(autoComplete: Boolean = true): MockProducer<String, String> {
        return MockProducer(
            autoComplete,
            StringSerializer(),
            StringSerializer()
        )
    }

    /**
     * Добавляет тестовое сообщение в MockConsumer
     */
    private fun MockConsumer<String, String>.addRecord(
        topic: String = testInTopic,
        partition: Int = testPartition,
        offset: Long,
        key: String?,
        value: String
    ) {
        val topicPartition = TopicPartition(topic, partition)
        addRecord(ConsumerRecord(topic, partition, offset, key, value))
        updateEndOffsets(mapOf(topicPartition to offset + 1))
    }

    /**
     * Создает логгер провайдер
     */
    private fun createLoggerProvider(): ArbScanLoggerProvider = ArbScanLoggerProvider()

    /**
     * Создает mock BusinessLogicProcessor, который ничего не делает
     */
    private fun createMockBusinessLogicProcessor(): BusinessLogicProcessor {
        return BusinessLogicProcessor { ctx ->
            // Простой mock - ничего не делает
        }
    }

    /**
     * Создает JSON с настройками по умолчанию
     */
    private fun createJson(): Json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should successfully process single message`() = runBlocking {
        // Given: настраиваем mock компоненты
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)

        // Счётчик вызовов бизнес-логики
        var businessLogicInvoked = false
        val mockBusinessLogicProcessor = BusinessLogicProcessor { _ ->
            businessLogicInvoked = true
        }

        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое JSON сообщение
        val testMessage = """{"requestType":"read","debug":{"mode":"stub","stub":"success"},"id":"test-id"}"""

        // Добавляем сообщение в consumer
        mockConsumer.addRecord(
            offset = 0L,
            key = "test-key",
            value = testMessage
        )

        // Создаем AppKafkaConsumer и AppKafkaProducer
        val appConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testInTopic)
        )

        val appProducer = AppKafkaProducer(
            producer = mockProducer,
            loggerProvider = loggerProvider,
            defaultTopic = testOutTopic
        )

        // Создаем контроллер
        val controller = AppKafkaController(
            consumer = appConsumer,
            producer = appProducer,
            businessLogicProcessor = mockBusinessLogicProcessor,
            loggerProvider = loggerProvider,
            json = json
        )

        // When: запускаем контроллер в отдельной корутине
        val job = launch {
            controller.start()
        }

        // Даем время на обработку сообщения
        delay(500)

        // Отменяем job
        job.cancelAndJoin()

        // Then: проверяем, что бизнес-логика была вызвана
        assertTrue(businessLogicInvoked, "Бизнес-логика должна была быть вызвана")

        // Проверяем, что producer получил сообщение
        val sentRecords = mockProducer.history()
        assertTrue(sentRecords.isNotEmpty(), "Producer должен был отправить хотя бы одно сообщение")
        assertEquals(testOutTopic, sentRecords[0].topic())

        // Cleanup
        controller.close()
    }

    @Test
    fun `should successfully process multiple messages`() = runBlocking {
        // Given: настраиваем mock компоненты
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)

        // Счётчик вызовов бизнес-логики
        var businessLogicInvokeCount = 0
        val mockBusinessLogicProcessor = BusinessLogicProcessor { _ ->
            businessLogicInvokeCount++
        }

        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Добавляем несколько тестовых сообщений
        val messagesCount = 3
        repeat(messagesCount) { index ->
            val testMessage =
                """{"requestType":"read","debug":{"mode":"stub","stub":"success"},"id":"test-id-$index"}"""
            mockConsumer.addRecord(
                offset = index.toLong(),
                key = "test-key-$index",
                value = testMessage
            )
        }

        // Создаем AppKafkaConsumer и AppKafkaProducer
        val appConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testInTopic)
        )

        val appProducer = AppKafkaProducer(
            producer = mockProducer,
            loggerProvider = loggerProvider,
            defaultTopic = testOutTopic
        )

        // Создаем контроллер
        val controller = AppKafkaController(
            consumer = appConsumer,
            producer = appProducer,
            businessLogicProcessor = mockBusinessLogicProcessor,
            loggerProvider = loggerProvider,
            json = json
        )

        // When: запускаем контроллер в отдельной корутине
        val job = launch {
            controller.start()
        }

        // Даем время на обработку всех сообщений
        delay(1000)

        // Отменяем job
        job.cancelAndJoin()

        // Then: проверяем, что бизнес-логика была вызвана для всех сообщений
        assertEquals(
            messagesCount,
            businessLogicInvokeCount,
            "Бизнес-логика должна была быть вызвана $messagesCount раз"
        )

        // Проверяем, что producer отправил все сообщения
        val sentRecords = mockProducer.history()
        assertEquals(messagesCount, sentRecords.size, "Producer должен был отправить $messagesCount сообщений")

        // Проверяем, что все сообщения отправлены в правильный топик
        sentRecords.forEach { record ->
            assertEquals(testOutTopic, record.topic())
        }

        // Cleanup
        controller.close()
    }

    @Test
    fun `should handle error during message processing`() = runBlocking {
        // Given: настраиваем mock компоненты
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)

        // Бизнес-логика, которая выбрасывает исключение
        val mockBusinessLogicProcessor = BusinessLogicProcessor { _ ->
            throw RuntimeException("Test error in business logic")
        }

        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое JSON сообщение
        val testMessage = """{"requestType":"read","debug":{"mode":"stub","stub":"success"},"id":"test-id"}"""

        // Добавляем сообщение в consumer
        mockConsumer.addRecord(
            offset = 0L,
            key = "test-key",
            value = testMessage
        )

        // Создаем AppKafkaConsumer и AppKafkaProducer
        val appConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testInTopic)
        )

        val appProducer = AppKafkaProducer(
            producer = mockProducer,
            loggerProvider = loggerProvider,
            defaultTopic = testOutTopic
        )

        // Создаем контроллер
        val controller = AppKafkaController(
            consumer = appConsumer,
            producer = appProducer,
            businessLogicProcessor = mockBusinessLogicProcessor,
            loggerProvider = loggerProvider,
            json = json
        )

        // When: запускаем контроллер в отдельной корутине
        val job = launch {
            controller.start()
        }

        // Даем время на обработку сообщения
        delay(500)

        // Отменяем job
        job.cancelAndJoin()

        // Then: проверяем, что контроллер не упал и продолжил работу
        // Ошибка должна быть залогирована, но контроллер должен продолжить работу
        // Проверяем, что producer отправил ответ (даже при ошибке)
        val sentRecords = mockProducer.history()
        assertTrue(sentRecords.isNotEmpty(), "Producer должен был отправить ответ даже при ошибке")
        assertEquals(testOutTopic, sentRecords[0].topic())

        // Cleanup
        controller.close()
    }

    @Test
    fun `should close resources correctly`() {
        // Given: настраиваем mock компоненты
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val mockBusinessLogicProcessor = createMockBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем AppKafkaConsumer и AppKafkaProducer
        val appConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testInTopic)
        )

        val appProducer = AppKafkaProducer(
            producer = mockProducer,
            loggerProvider = loggerProvider,
            defaultTopic = testOutTopic
        )

        // Создаем контроллер
        val controller = AppKafkaController(
            consumer = appConsumer,
            producer = appProducer,
            businessLogicProcessor = mockBusinessLogicProcessor,
            loggerProvider = loggerProvider,
            json = json
        )

        // When: закрываем контроллер
        controller.close()

        // Then: проверяем, что consumer и producer закрыты
        assertTrue(mockConsumer.closed(), "Consumer должен быть закрыт")
        assertTrue(mockProducer.closed(), "Producer должен быть закрыт")
    }
}
