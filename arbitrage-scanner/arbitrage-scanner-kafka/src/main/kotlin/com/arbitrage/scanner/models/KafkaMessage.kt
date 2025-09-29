package com.arbitrage.scanner.models

import org.apache.kafka.clients.consumer.ConsumerRecord

/**
 * Представление Kafka сообщения с извлеченными метаданными из заголовков
 *
 * @property correlationId ID для сопоставления запроса и ответа
 * @property requestType Тип запроса (для роутинга и обработки)
 * @property timestamp Временная метка создания сообщения
 * @property payload Тело сообщения в формате JSON
 */
data class KafkaMessage(
    val correlationId: String,
    val requestType: String,
    val timestamp: Long,
    val payload: String
)

/**
 * Преобразует ConsumerRecord в KafkaMessage, извлекая данные из заголовков
 *
 * Fallback стратегии:
 * - correlation-id: использует key записи, если заголовок отсутствует, иначе "unknown"
 * - request-type: использует "UNKNOWN", если заголовок отсутствует
 * - timestamp: использует текущее время, если заголовок отсутствует
 */
fun ConsumerRecord<String, String>.toKafkaMessage(): KafkaMessage {
    val correlationId = headers().lastHeader("correlation-id")
        ?.value()?.decodeToString() ?: key() ?: "unknown"

    val requestType = headers().lastHeader("request-type")
        ?.value()?.decodeToString() ?: "UNKNOWN"

    val timestamp = headers().lastHeader("timestamp")
        ?.value()?.decodeToString()?.toLongOrNull() ?: System.currentTimeMillis()

    return KafkaMessage(
        correlationId = correlationId,
        requestType = requestType,
        timestamp = timestamp,
        payload = value()
    )
}