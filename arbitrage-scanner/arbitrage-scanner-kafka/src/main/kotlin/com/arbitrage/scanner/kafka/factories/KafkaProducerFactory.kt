package com.arbitrage.scanner.kafka.factories

import com.arbitrage.scanner.kafka.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

/**
 * Фабрика для создания KafkaProducer.
 * Инкапсулирует логику создания и конфигурации Kafka Producer клиента.
 *
 * @property config конфигурация Kafka
 */
class KafkaProducerFactory(
    private val config: KafkaConfig
) {
    /**
     * Создает и возвращает настроенный экземпляр KafkaProducer.
     *
     * @return настроенный KafkaProducer<String, String>
     */
    fun createProducer(): KafkaProducer<String, String> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to config.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
        )

        return KafkaProducer(props, StringSerializer(), StringSerializer())
    }
}
