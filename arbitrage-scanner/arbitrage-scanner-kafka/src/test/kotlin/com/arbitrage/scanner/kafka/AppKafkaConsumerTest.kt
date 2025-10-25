package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit тесты для AppKafkaConsumer с использованием MockConsumer
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
     * Создает логгер провайдер
     */
    private fun createMockLoggerProvider(): ArbScanLoggerProvider = ArbScanLoggerProvider()

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
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val loggerProvider = createMockLoggerProvider()

        val appKafkaConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testTopic),
            dispatcher = testDispatcher
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

        // Cleanup
        appKafkaConsumer.close()
    }

    @Test
    fun `should receive multiple messages in single poll`() = runTest {
        // Given: MockConsumer с несколькими сообщениями
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val loggerProvider = createMockLoggerProvider()

        val appKafkaConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testTopic),
            dispatcher = testDispatcher
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
}