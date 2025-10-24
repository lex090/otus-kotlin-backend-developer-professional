package com.arbitrage.scanner.kafka.config

import kotlinx.serialization.Serializable

@Serializable
data class ServiceConfig(
    val kafka: KafkaConfig
)

@Serializable
data class KafkaConfig(
    val host: String,
    val port: Int,
    val inTopic: String,
    val outTopic: String,
    val groupId: String,
) {
    val bootstrapServers: String
        get() = "$host:$port"
}
