package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.IResponse

/**
 * Интерфейс для отправки Response сообщений в Kafka
 *
 * Обеспечивает абстракцию над конкретной реализацией Kafka Producer,
 * следуя принципу Dependency Inversion (SOLID).
 */
interface ResponseProducer {
    /**
     * Отправляет ответ в Kafka топик
     *
     * @param correlationId ID для сопоставления запроса и ответа
     * @param response ответ для отправки
     */
    suspend fun sendResponse(
        correlationId: String,
        response: IResponse
    )

    /**
     * Закрывает producer и освобождает ресурсы
     */
    suspend fun close()
}