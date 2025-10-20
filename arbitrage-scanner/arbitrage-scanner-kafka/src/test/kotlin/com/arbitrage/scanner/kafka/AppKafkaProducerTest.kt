package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit тесты для AppKafkaProducer с использованием MockProducer
 */
class AppKafkaProducerTest {

    private val testTopic = "test-topic"

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
     * Создает mock логгер провайдер
     */
    private fun createMockLoggerProvider(): ArbScanLoggerProvider {
        val mockLogger = mockk<ArbScanLogWrapper>(relaxed = true)
        return mockk<ArbScanLoggerProvider> {
            every { logger(any<kotlin.reflect.KClass<*>>()) } returns mockLogger
        }
    }

    @Test
    fun `should successfully send message and return metadata`() = runTest {
        // Given: MockProducer с автоматическим подтверждением
        val mockProducer = createMockProducer(autoComplete = true)
        val loggerProvider = createMockLoggerProvider()

        val appKafkaProducer = AppKafkaProducer(
            producer = mockProducer,
            loggerProvider = loggerProvider,
            defaultTopic = testTopic
        )

        val testMessage = "test-message"

        // When: Отправляем сообщение
        val metadata = appKafkaProducer.send(testMessage)

        // Then: Проверяем что сообщение отправлено
        assertNotNull(metadata)
        assertEquals(testTopic, metadata.topic())
        assertEquals(0, metadata.partition()) // MockProducer использует partition 0 по умолчанию
        assertEquals(0L, metadata.offset()) // Первое сообщение имеет offset 0

        // Проверяем что сообщение действительно было отправлено в MockProducer
        val sentRecords = mockProducer.history()
        assertEquals(1, sentRecords.size)

        val sentRecord = sentRecords[0]
        assertEquals(testTopic, sentRecord.topic())
        assertEquals(testMessage, sentRecord.value())

        // Cleanup
        appKafkaProducer.close()
    }
}