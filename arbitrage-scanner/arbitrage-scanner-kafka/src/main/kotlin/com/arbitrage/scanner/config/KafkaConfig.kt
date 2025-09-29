package com.arbitrage.scanner.config

/**
 * Корневая конфигурация приложения
 */
data class AppConfig(
    val kafka: KafkaConfig = KafkaConfig()
)

/**
 * Общие настройки Kafka
 *
 * @property bootstrapServers Список адресов Kafka брокеров (например, "localhost:9092")
 * @property consumer Конфигурация consumer
 * @property producer Конфигурация producer
 */
data class KafkaConfig(
    val bootstrapServers: String = "localhost:9092",
    val consumer: ConsumerConfig = ConsumerConfig(),
    val producer: ProducerConfig = ProducerConfig()
)

/**
 * Настройки Kafka Consumer
 *
 * @property groupId Идентификатор группы consumer'ов
 * @property topics Список топиков для подписки
 * @property autoOffsetReset Стратегия чтения при отсутствии offset'а (earliest, latest)
 * @property enableAutoCommit Автоматический commit offset'ов
 * @property autoCommitIntervalMs Интервал автокоммита в миллисекундах
 * @property sessionTimeoutMs Таймаут сессии в миллисекундах
 * @property maxPollRecords Максимальное количество записей за один poll
 * @property properties Дополнительные свойства consumer'а
 */
data class ConsumerConfig(
    val groupId: String = "arbitrage-scanner-group",
    val topics: List<String> = listOf("arbitrage-requests"),
    val autoOffsetReset: String = "earliest",
    val enableAutoCommit: Boolean = true,
    val autoCommitIntervalMs: Int = 5000,
    val sessionTimeoutMs: Int = 30000,
    val maxPollRecords: Int = 500,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Настройки Kafka Producer
 *
 * @property clientId Идентификатор клиента producer'а
 * @property topics Конфигурация топиков для отправки
 * @property acks Режим подтверждения записи (0, 1, all)
 * @property retries Количество повторных попыток при ошибке
 * @property batchSize Размер батча в байтах
 * @property lingerMs Время ожидания перед отправкой батча
 * @property compressionType Тип сжатия (none, gzip, snappy, lz4, zstd)
 * @property maxInFlightRequestsPerConnection Максимальное количество неподтвержденных запросов
 * @property properties Дополнительные свойства producer'а
 */
data class ProducerConfig(
    val clientId: String = "arbitrage-scanner-producer",
    val topics: ProducerTopicsConfig = ProducerTopicsConfig(),
    val acks: String = "all",
    val retries: Int = 3,
    val batchSize: Int = 16384,
    val lingerMs: Int = 10,
    val compressionType: String = "gzip",
    val maxInFlightRequestsPerConnection: Int = 5,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Конфигурация топиков для producer'а
 *
 * @property responses Топик для отправки ответов
 */
data class ProducerTopicsConfig(
    val responses: String = "arbitrage-responses"
)