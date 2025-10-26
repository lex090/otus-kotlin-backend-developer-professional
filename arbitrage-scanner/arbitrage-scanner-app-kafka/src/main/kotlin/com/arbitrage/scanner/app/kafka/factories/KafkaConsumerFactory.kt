package com.arbitrage.scanner.app.kafka.factories

import com.arbitrage.scanner.app.kafka.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

/**
 * Фабрика для создания KafkaConsumer.
 * Инкапсулирует логику создания и конфигурации Kafka Consumer клиента.
 *
 * @property config конфигурация Kafka
 */
class KafkaConsumerFactory(
    private val config: KafkaConfig
) {
    /**
     * Создает и возвращает настроенный экземпляр KafkaConsumer.
     *
     * @return настроенный KafkaConsumer<String, String>
     */
    fun createConsumer(): KafkaConsumer<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to config.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to config.groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        )

        return KafkaConsumer(props, StringDeserializer(), StringDeserializer())
    }
}
