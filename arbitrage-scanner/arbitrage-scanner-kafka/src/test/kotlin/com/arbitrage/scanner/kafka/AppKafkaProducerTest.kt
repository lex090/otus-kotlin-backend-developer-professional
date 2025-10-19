package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.kafka.config.KafkaConfig
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты для AppKafkaProducer.
 */
class AppKafkaProducerTest {

    private val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)

    private val testConfig = KafkaConfig(
        host = "localhost",
        port = 9092,
        inTopic = "test-input-topic",
        outTopic = "test-output-topic",
        groupId = "test-producer-group"
    )

    @Test
    fun `должен корректно создаться с конфигурацией`() {
        // Act & Assert
        val producer = AppKafkaProducer(testConfig, loggerProvider)

        assertEquals(producer::class.simpleName, "AppKafkaProducer")
        producer.close()
    }

    @Test
    fun `должен корректно создаться с кастомным топиком`() {
        // Act & Assert
        val customTopic = "custom-topic"
        val producer = AppKafkaProducer(testConfig, loggerProvider, defaultTopic = customTopic)

        assertTrue(producer::class.simpleName == "AppKafkaProducer")
        producer.close()
    }
}
