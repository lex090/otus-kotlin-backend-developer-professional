package com.arbitrage.scanner.app.kafka

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorSimpleImpl
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebug
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugMode
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubs
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilter
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.DexToCexSimpleArbitrageOpportunity
import com.arbitrage.scanner.fromResponseJsonString
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.toRequestJsonString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit тесты для AppKafkaController с использованием MockConsumer и MockProducer
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
     * Создает BusinessLogicProcessorSimpleImpl для тестирования
     */
    private fun createBusinessLogicProcessor() = BusinessLogicProcessorSimpleImpl()

    /**
     * Создает JSON с настройками по умолчанию
     */
    private fun createJson(): Json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should successfully process read request`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val businessLogicProcessor = createBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое сообщение для read запроса из API модели
        val request = ArbitrageOpportunityReadRequest(
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
            ),
            id = "test-id"
        )
        val testMessage = json.toRequestJsonString(request)

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
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
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

        // Then: проверяем, что producer получил сообщение
        val sentRecords = mockProducer.history()
        assertTrue(sentRecords.isNotEmpty(), "Producer должен был отправить хотя бы одно сообщение")
        assertEquals(testOutTopic, sentRecords[0].topic())

        // Проверяем содержимое ответа
        val responseJson = sentRecords[0].value()
        assertNotNull(responseJson, "Ответ не должен быть null")

        val response = json.fromResponseJsonString<ArbitrageOpportunityReadResponse>(responseJson)
        assertNotNull(response.arbitrageOpportunity, "Арбитражная возможность не должна быть null")

        val opportunity = response.arbitrageOpportunity as? DexToCexSimpleArbitrageOpportunity
        assertNotNull(opportunity, "Арбитражная возможность должна быть типа DexToCexSimpleArbitrageOpportunity")
        assertEquals("123", opportunity.id, "ID арбитражной возможности должен совпадать со stub")
        assertEquals(12313.0, opportunity.spread, "Spread должен совпадать со stub")

        // Cleanup
        controller.close()
    }

    @Test
    fun `should successfully process multiple messages`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val businessLogicProcessor = createBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Добавляем несколько тестовых сообщений
        val messagesCount = 3
        repeat(messagesCount) { index ->
            val request = ArbitrageOpportunityReadRequest(
                debug = ArbitrageOpportunityDebug(
                    mode = ArbitrageOpportunityRequestDebugMode.STUB,
                    stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
                ),
                id = "test-id-$index"
            )
            val testMessage = json.toRequestJsonString(request)
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
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
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
    fun `should handle error during message processing`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)

        // Используем BusinessLogicProcessor, который выбрасывает исключение
        val businessLogicProcessor = BusinessLogicProcessor { _ ->
            throw RuntimeException("Test error in business logic")
        }

        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое сообщение для read запроса из API модели
        val request = ArbitrageOpportunityReadRequest(
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
            ),
            id = "test-id"
        )
        val testMessage = json.toRequestJsonString(request)

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
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
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
    fun `should close resources correctly`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val businessLogicProcessor = createBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем AppKafkaConsumer и AppKafkaProducer
        val appConsumer = AppKafkaConsumer(
            consumer = mockConsumer,
            loggerProvider = loggerProvider,
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
            loggerProvider = loggerProvider,
            json = json
        )

        // When: закрываем контроллер
        controller.close()

        // Then: проверяем, что consumer и producer закрыты
        assertTrue(mockConsumer.closed(), "Consumer должен быть закрыт")
        assertTrue(mockProducer.closed(), "Producer должен быть закрыт")
    }

    @Test
    fun `should successfully process search request`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val businessLogicProcessor = createBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое сообщение для search запроса из API модели
        val request = ArbitrageOpportunitySearchRequest(
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
            ),
            filter = ArbitrageOpportunitySearchFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = null
            )
        )
        val testMessage = json.toRequestJsonString(request)

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
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
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

        // Then: проверяем, что producer получил сообщение
        val sentRecords = mockProducer.history()
        assertTrue(sentRecords.isNotEmpty(), "Producer должен был отправить хотя бы одно сообщение")
        assertEquals(testOutTopic, sentRecords[0].topic())

        // Проверяем содержимое ответа
        val responseJson = sentRecords[0].value()
        assertNotNull(responseJson, "Ответ не должен быть null")

        val response = json.fromResponseJsonString<ArbitrageOpportunitySearchResponse>(responseJson)
        assertNotNull(response.arbitrageOpportunities, "Список арбитражных возможностей не должен быть null")
        assertTrue(response.arbitrageOpportunities!!.isNotEmpty(), "Список должен содержать хотя бы одну возможность")

        val opportunity = response.arbitrageOpportunities!![0] as? DexToCexSimpleArbitrageOpportunity
        assertNotNull(opportunity, "Арбитражная возможность должна быть типа DexToCexSimpleArbitrageOpportunity")
        assertEquals("123", opportunity.id, "ID арбитражной возможности должен совпадать со stub")

        // Cleanup
        controller.close()
    }

    @Test
    fun `should successfully process recalculate request`() = runTest {
        // Given: настраиваем mock компоненты
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val mockConsumer = createMockConsumer()
        val mockProducer = createMockProducer(autoComplete = true)
        val businessLogicProcessor = createBusinessLogicProcessor()
        val loggerProvider = createLoggerProvider()
        val json = createJson()

        // Создаем тестовое сообщение для recalculate запроса из API модели
        val request = ArbitrageOpportunityRecalculateRequest(
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
            )
        )
        val testMessage = json.toRequestJsonString(request)

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
            topics = listOf(testInTopic),
            dispatcher = testDispatcher
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
            businessLogicProcessor = businessLogicProcessor,
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

        // Then: проверяем, что producer получил сообщение
        val sentRecords = mockProducer.history()
        assertTrue(sentRecords.isNotEmpty(), "Producer должен был отправить хотя бы одно сообщение")
        assertEquals(testOutTopic, sentRecords[0].topic())

        // Проверяем содержимое ответа
        val responseJson = sentRecords[0].value()
        assertNotNull(responseJson, "Ответ не должен быть null")

        val response = json.fromResponseJsonString<ArbitrageOpportunityRecalculateResponse>(responseJson)
        assertNotNull(response.opportunitiesCount, "Количество возможностей не должно быть null")
        assertNotNull(response.processingTimeMs, "Время обработки не должно быть null")
        assertEquals(1, response.opportunitiesCount, "Количество возможностей должно совпадать со stub")
        assertEquals(100L, response.processingTimeMs, "Время обработки должно совпадать со stub")

        // Cleanup
        controller.close()
    }
}
