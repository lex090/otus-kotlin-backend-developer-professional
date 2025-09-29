package com.arbitrage.scanner.consumers

import com.arbitrage.scanner.config.ConsumerConfig
import com.arbitrage.scanner.config.KafkaConfig
import com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import com.arbitrage.scanner.models.toKafkaMessage
import com.arbitrage.scanner.processors.KafkaMessageProcessor
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig as ApacheConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kafka Consumer для обработки запросов Arbitrage Scanner
 *
 * Реализует:
 * - Подписку на топики запросов
 * - Циклическое чтение сообщений с корутинами
 * - Обработку через KafkaMessageProcessor
 * - Graceful shutdown с корректным закрытием ресурсов
 *
 * @property config конфигурация consumer
 * @property kafkaConfig общая конфигурация Kafka
 * @property messageProcessor процессор сообщений
 * @property logger логгер
 */
class ArbitrageKafkaConsumer(
    private val config: ConsumerConfig,
    private val kafkaConfig: KafkaConfig,
    private val messageProcessor: KafkaMessageProcessor,
    private val logger: ArbScanLogWrapper
) {
    private val consumer: KafkaConsumer<String, String>
    private val isRunning = AtomicBoolean(false)
    private var consumerJob: Job? = null

    init {
        val props = Properties().apply {
            put(ApacheConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
            put(ApacheConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ApacheConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ApacheConsumerConfig.GROUP_ID_CONFIG, config.groupId)
            put(ApacheConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)
            put(ApacheConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.enableAutoCommit)
            put(ApacheConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.autoCommitIntervalMs)
            put(ApacheConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.sessionTimeoutMs)
            put(ApacheConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.maxPollRecords)

            // Добавляем дополнительные свойства из конфигурации
            config.properties.forEach { (key, value) ->
                put(key, value)
            }
        }

        consumer = KafkaConsumer(props)
        consumer.subscribe(config.topics)

        logger.info(
            msg = "Kafka consumer initialized",
            marker = "KAFKA_CONSUMER",
            data = "groupId=${config.groupId}, topics=${config.topics}"
        )
    }

    /**
     * Запускает consumer для чтения и обработки сообщений
     */
    fun start(scope: CoroutineScope) {
        if (isRunning.getAndSet(true)) {
            logger.info(msg = "Consumer already running", marker = "KAFKA_CONSUMER")
            return
        }

        logger.info(msg = "Starting Kafka consumer", marker = "KAFKA_CONSUMER")

        consumerJob = scope.launch(Dispatchers.IO) {
            try {
                while (isActive && isRunning.get()) {
                    val records = consumer.poll(Duration.ofMillis(1000))

                    if (records.isEmpty) {
                        continue
                    }

                    logger.debug(
                        msg = "Polled messages from Kafka",
                        marker = "KAFKA_CONSUMER",
                        data = "count=${records.count()}"
                    )

                    records.forEach { record ->
                        try {
                            val kafkaMessage = record.toKafkaMessage()
                            messageProcessor.processMessage(kafkaMessage)
                        } catch (e: Exception) {
                            logger.error(
                                msg = "Error processing message",
                                marker = "KAFKA_CONSUMER",
                                data = "offset=${record.offset()}, partition=${record.partition()}",
                                e = e
                            )
                        }
                    }

                    // Manual commit если auto-commit выключен
                    if (!config.enableAutoCommit) {
                        consumer.commitSync()
                    }
                }
            } catch (e: CancellationException) {
                logger.info(msg = "Consumer cancelled", marker = "KAFKA_CONSUMER")
            } catch (e: Exception) {
                logger.error(msg = "Consumer error", marker = "KAFKA_CONSUMER", e = e)
                throw e
            }
        }
    }

    /**
     * Останавливает consumer с graceful shutdown
     */
    suspend fun stop() {
        logger.info(msg = "Stopping Kafka consumer", marker = "KAFKA_CONSUMER")

        isRunning.set(false)
        consumerJob?.cancelAndJoin()

        withContext(Dispatchers.IO) {
            consumer.close(Duration.ofSeconds(10))
        }

        logger.info(msg = "Kafka consumer stopped", marker = "KAFKA_CONSUMER")
    }
}