package com.arbitrage.scanner.kafka.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

object KafkaConfigLoader {

    fun load(): KafkaConfig {
        val serviceConfig = ConfigLoaderBuilder.default()
            .addResourceSource("/application.yaml")
            .build()
            .loadConfigOrThrow<ServiceConfig>()

        return serviceConfig.kafka
    }
}
