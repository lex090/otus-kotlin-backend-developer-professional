package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorSimpleImpl
import com.arbitrage.scanner.kafka.config.KafkaConfig
import com.arbitrage.scanner.kafka.config.ServiceConfig
import com.arbitrage.scanner.kafka.factories.KafkaConsumerFactory
import com.arbitrage.scanner.kafka.factories.KafkaProducerFactory
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Конфигурация модулей Koin для Kafka приложения.
 */

/**
 * Модуль для конфигурации JSON сериализации.
 */
val jsonModule: Module = module {
    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

/**
 * Модуль для бизнес-логики.
 */
val businessLogicProcessorModule: Module = module {
    single<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
}

/**
 * Модуль для логирования.
 */
val loggingModule: Module = module {
    single<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}

/**
 * Модуль для конфигурации Kafka.
 */
val kafkaConfigModule: Module = module {
    single<KafkaConfig> {
        val serviceConfig = ConfigLoaderBuilder.default()
            .addResourceSource("/application.yaml")
            .build()
            .loadConfigOrThrow<ServiceConfig>()
        serviceConfig.kafka
    }
}

/**
 * Модуль для фабрик Kafka клиентов.
 */
val kafkaFactoriesModule: Module = module {
    single<KafkaConsumerFactory> {
        KafkaConsumerFactory(config = get())
    }

    single<KafkaProducerFactory> {
        KafkaProducerFactory(config = get())
    }

    single<Consumer<String, String>> {
        get<KafkaConsumerFactory>().createConsumer()
    }

    single<Producer<String, String>> {
        get<KafkaProducerFactory>().createProducer()
    }
}

/**
 * Модуль для Kafka компонентов.
 */
val kafkaModule: Module = module {
    single<AppKafkaConsumer> {
        val config: KafkaConfig = get()
        AppKafkaConsumer(
            consumer = get(),
            loggerProvider = get(),
            topics = listOf(config.inTopic)
        )
    }

    single<AppKafkaProducer> {
        val config: KafkaConfig = get()
        AppKafkaProducer(
            producer = get(),
            loggerProvider = get(),
            defaultTopic = config.outTopic
        )
    }

    single<AppKafkaController> {
        AppKafkaController(
            consumer = get(),
            producer = get(),
            businessLogicProcessor = get(),
            loggerProvider = get(),
            json = get()
        )
    }
}

/**
 * Все модули приложения.
 */
val allModules = listOf(
    jsonModule,
    businessLogicProcessorModule,
    loggingModule,
    kafkaConfigModule,
    kafkaFactoriesModule,
    kafkaModule
)
