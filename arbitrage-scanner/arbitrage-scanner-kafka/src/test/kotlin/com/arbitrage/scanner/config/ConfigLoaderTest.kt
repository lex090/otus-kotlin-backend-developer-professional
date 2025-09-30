package com.arbitrage.scanner.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Тесты для загрузчика конфигурации
 */
class ConfigLoaderTest {

    @Test
    fun `should load config from application yaml`() {
        // given / when
        val config = ConfigLoader.loadConfig()

        // then
        assertNotNull(config)
        assertNotNull(config.kafka)
    }

    @Test
    fun `should load kafka bootstrap servers`() {
        // given / when
        val config = ConfigLoader.loadConfig()

        // then
        assertEquals("localhost:9092", config.kafka.bootstrapServers)
    }

    @Test
    fun `should load consumer configuration`() {
        // given / when
        val config = ConfigLoader.loadConfig()
        val consumer = config.kafka.consumer

        // then
        assertEquals("arbitrage-scanner-group", consumer.groupId)
        assertEquals(listOf("arbitrage-scanner-in"), consumer.topics)
        assertEquals("earliest", consumer.autoOffsetReset)
        assertEquals(true, consumer.enableAutoCommit)
        assertEquals(5000, consumer.autoCommitIntervalMs)
        assertEquals(30000, consumer.sessionTimeoutMs)
        assertEquals(500, consumer.maxPollRecords)
    }

    @Test
    fun `should load producer configuration`() {
        // given / when
        val config = ConfigLoader.loadConfig()
        val producer = config.kafka.producer

        // then
        assertEquals("arbitrage-scanner-producer", producer.clientId)
        assertEquals("arbitrage-scanner-out", producer.topics.responses)
        assertEquals("all", producer.acks)
        assertEquals(3, producer.retries)
        assertEquals(16384, producer.batchSize)
        assertEquals(10, producer.lingerMs)
        assertEquals("gzip", producer.compressionType)
        assertEquals(5, producer.maxInFlightRequestsPerConnection)
    }
}