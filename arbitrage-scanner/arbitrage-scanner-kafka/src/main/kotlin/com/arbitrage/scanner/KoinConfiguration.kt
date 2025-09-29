package com.arbitrage.scanner

import com.arbitrage.scanner.config.AppConfig
import com.arbitrage.scanner.config.ConfigLoader
import com.arbitrage.scanner.consumers.ArbitrageKafkaConsumer
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import com.arbitrage.scanner.processors.KafkaMessageProcessor
import com.arbitrage.scanner.producers.KafkaResponseProducer
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Конфигурация модулей Koin для Kafka транспорта
 */

/**
 * Модуль конфигурации приложения
 */
val configModule = module {
    single<AppConfig> { ConfigLoader.loadConfig() }
    single { get<AppConfig>().kafka }
}

/**
 * Модуль JSON сериализации
 */
val jsonModule = module {
    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

/**
 * Модуль бизнес-логики
 */
val businessLogicModule = module {
    single<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
}

/**
 * Модуль логирования
 */
val loggingModule = module {
    single<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}

/**
 * Модуль Kafka producer
 */
val kafkaProducerModule = module {
    single<ResponseProducer> {
        KafkaResponseProducer(
            config = get<AppConfig>().kafka.producer,
            kafkaConfig = get(),
            json = get(),
            logger = get<ArbScanLoggerProvider>().logger("KafkaResponseProducer")
        )
    }
}

/**
 * Модуль процессоров сообщений
 */
val messageProcessorModule = module {
    single<KafkaMessageProcessor> {
        KafkaMessageProcessor(
            json = get(),
            businessLogicProcessor = get(),
            responseProducer = get(),
            loggerProvider = get()
        )
    }
}

/**
 * Модуль Kafka consumer
 */
val kafkaConsumerModule = module {
    single<ArbitrageKafkaConsumer> {
        ArbitrageKafkaConsumer(
            config = get<AppConfig>().kafka.consumer,
            kafkaConfig = get(),
            messageProcessor = get(),
            logger = get<ArbScanLoggerProvider>().logger("ArbitrageKafkaConsumer")
        )
    }
}

/**
 * Все модули приложения
 */
val allModules: List<Module> = listOf(
    configModule,
    jsonModule,
    businessLogicModule,
    loggingModule,
    kafkaProducerModule,
    messageProcessorModule,
    kafkaConsumerModule
)