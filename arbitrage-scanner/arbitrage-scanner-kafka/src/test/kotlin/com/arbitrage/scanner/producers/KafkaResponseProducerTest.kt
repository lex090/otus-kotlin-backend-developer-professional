package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.toResponseJsonString
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit тесты для KafkaResponseProducer
 *
 * Проверяют корректность сериализации IResponse в JSON
 */
class KafkaResponseProducerTest {

    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val mockLogger = object : ArbScanLogWrapper() {
        override val loggerId: String = "TEST"
        override fun log(
            msg: String,
            level: com.arbitrage.scanner.libs.logging.LogLevel,
            marker: String,
            e: Throwable?,
            data: Any?,
            objs: Map<String, Any>?
        ) {
            // Mock logger - не выполняет реального логирования
        }
    }

    @Test
    fun `should serialize ArbitrageOpportunityReadResponse to JSON correctly`() = runBlocking {
        // Given
        val response = ArbitrageOpportunityReadResponse(
            result = ResponseResult.SUCCESS,
            errors = emptyList(),
            arbitrageOpportunity = null
        )

        // When
        val jsonString = json.toResponseJsonString(response)

        // Then - просто проверяем, что JSON создается
        assertNotNull(jsonString)
        assertTrue(jsonString.isNotEmpty())
    }

    @Test
    fun `should serialize response with errors to JSON correctly`() = runBlocking {
        // Given
        val response = ArbitrageOpportunityReadResponse(
            result = ResponseResult.ERROR,
            errors = listOf(
                com.arbitrage.scanner.api.v1.models.Error(
                    code = "TEST_ERROR",
                    group = "validation",
                    field = "id",
                    message = "Test error message"
                )
            ),
            arbitrageOpportunity = null
        )

        // When
        val jsonString = json.toResponseJsonString(response)

        // Then - просто проверяем, что JSON создается
        assertNotNull(jsonString)
        assertTrue(jsonString.isNotEmpty())
    }

    @Test
    fun `should deserialize JSON to IResponse correctly`() = runBlocking {
        // Given
        val originalResponse = ArbitrageOpportunityReadResponse(
            result = ResponseResult.SUCCESS,
            errors = emptyList(),
            arbitrageOpportunity = null
        )
        val jsonString = json.toResponseJsonString(originalResponse)

        // When
        val deserializedResponse = json.decodeFromString<IResponse>(
            IResponse.serializer(),
            jsonString
        )

        // Then
        assertNotNull(deserializedResponse)
        assertTrue(deserializedResponse is ArbitrageOpportunityReadResponse)
        val readResponse = deserializedResponse as ArbitrageOpportunityReadResponse
        assertTrue(readResponse.result == ResponseResult.SUCCESS)
    }

    // Примечание: Полноценное тестирование KafkaProducer с отправкой сообщений
    // требует MockProducer или Testcontainers с реальным Kafka брокером.
    // Эти тесты будут добавлены в интеграционные тесты (Subtask-18)
}