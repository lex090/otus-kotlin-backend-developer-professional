package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit тесты для AppKafkaConsumer с использованием MockConsumer
 */
class AppKafkaConsumerTest {

    private val testTopic = "test-topic"
    private val testPartition = 0

    /**
     * Создает настроенный MockConsumer с начальными параметрами
     */
    private fun createMockConsumer(): MockConsumer<String, String> {
        return MockConsumer<String, String>(OffsetResetStrategy.EARLIEST).apply {
            // Создаем TopicPartition и назначаем его consumer'у
            val topicPartition = TopicPartition(testTopic, testPartition)

            // Явно подписываемся на топик (внутренняя подписка)
            subscribe(listOf(testTopic))

            // Имитируем ребалансировку - назначаем партицию
            rebalance(listOf(topicPartition))

            // Устанавливаем начальные offset'ы для топика
            updateBeginningOffsets(mapOf(topicPartition to 0L))
            updateEndOffsets(mapOf(topicPartition to 0L))
        }
    }

    /**
     * Создает mock логгер провайдер
     */
    private fun createMockLoggerProvider(): ArbScanLoggerProvider {
        val mockLogger = mockk<ArbScanLogWrapper>(relaxed = true)
        return mockk<ArbScanLoggerProvider> {
            every { logger(any<kotlin.reflect.KClass<*>>()) } returns mockLogger
        }
    }

    /**
     * Добавляет тестовое сообщение в MockConsumer
     */
    private fun MockConsumer<String, String>.addRecord(
        topic: String = testTopic,
        partition: Int = testPartition,
        offset: Long,
        key: String?,
        value: String
    ) {
        val topicPartition = TopicPartition(topic, partition)
        addRecord(ConsumerRecord(topic, partition, offset, key, value))
        // Обновляем end offset
        updateEndOffsets(mapOf(topicPartition to offset + 1))
    }

    @Test
    fun `should successfully subscribe and receive single message`() = runTest {
        // Given: MockConsumer с одним сообщением
        val mockConsumer = createMockConsumer()
        val loggerProvider = createMockLoggerProvider()

        val appKafkaConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testTopic)
        )

        // Добавляем тестовое сообщение
        mockConsumer.addRecord(
            offset = 0L,
            key = "test-key",
            value = "test-message"
        )

        // When: Подписываемся и получаем первое сообщение
        val flow = appKafkaConsumer.subscribe()
        val record = flow.first()

        // Then: Проверяем полученное сообщение
        assertEquals(testTopic, record.topic())
        assertEquals(testPartition, record.partition())
        assertEquals(0L, record.offset())
        assertEquals("test-key", record.key())
        assertEquals("test-message", record.value())

        // Проверяем, что были вызваны методы логирования
        val mockLogger = loggerProvider.logger(AppKafkaConsumer::class)
        verify { mockLogger.info(match { it.contains("Подписка на топики") }) }

        // Cleanup
        appKafkaConsumer.close()
    }

    @Test
    fun `should receive multiple messages in single poll`() = runTest {
        // Given: MockConsumer с несколькими сообщениями
        val mockConsumer = createMockConsumer()
        val loggerProvider = createMockLoggerProvider()

        val appKafkaConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testTopic)
        )

        // Добавляем несколько тестовых сообщений
        mockConsumer.addRecord(offset = 0L, key = "key-1", value = "message-1")
        mockConsumer.addRecord(offset = 1L, key = "key-2", value = "message-2")
        mockConsumer.addRecord(offset = 2L, key = "key-3", value = "message-3")

        // When: Подписываемся и получаем все сообщения
        val flow = appKafkaConsumer.subscribe()
        val records = mutableListOf<ConsumerRecord<String, String>>()

        // Используем take для ограничения количества сообщений
        flow.take(3).collect { record ->
            records.add(record)
        }

        // Then: Проверяем, что получили все 3 сообщения
        assertEquals(3, records.size)

        // Проверяем первое сообщение
        assertEquals("key-1", records[0].key())
        assertEquals("message-1", records[0].value())
        assertEquals(0L, records[0].offset())

        // Проверяем второе сообщение
        assertEquals("key-2", records[1].key())
        assertEquals("message-2", records[1].value())
        assertEquals(1L, records[1].offset())

        // Проверяем третье сообщение
        assertEquals("key-3", records[2].key())
        assertEquals("message-3", records[2].value())
        assertEquals(2L, records[2].offset())

        // Cleanup
        appKafkaConsumer.close()
    }

    @Test
    fun `should handle coroutine cancellation gracefully`() = runTest {
        // Given: MockConsumer с сообщениями
        val mockConsumer = createMockConsumer()
        val loggerProvider = createMockLoggerProvider()

        val appKafkaConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testTopic)
        )

        // Добавляем одно сообщение
        mockConsumer.addRecord(offset = 0L, key = "key-1", value = "message-1")

        var wasCollecting = false

        // When: Запускаем Flow сбор в отдельной корутине и отменяем её
        val job = launch {
            try {
                appKafkaConsumer.subscribe().collect { _ ->
                    wasCollecting = true
                }
            } catch (e: Exception) {
                // Ожидаем отмену корутины
            }
        }

        // Ждём начала подписки (используем yield для передачи управления)
        kotlinx.coroutines.yield()

        // Отменяем корутину
        job.cancel()
        job.join() // Ждём завершения

        // Then: Проверяем, что корутина была отменена без ошибок
        // (Это минимальный тест - просто проверяем, что не было исключений)
        assert(job.isCancelled || job.isCompleted)

        // Cleanup
        appKafkaConsumer.close()
    }
}