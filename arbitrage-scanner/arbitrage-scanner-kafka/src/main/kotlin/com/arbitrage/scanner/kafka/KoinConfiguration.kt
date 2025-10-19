package com.arbitrage.scanner.kafka

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.BusinessLogicProcessorSimpleImpl
import com.arbitrage.scanner.kafka.config.KafkaConfig
import com.arbitrage.scanner.kafka.config.ServiceConfig
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import kotlinx.serialization.json.Json
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
 * Модуль для Kafka компонентов.
 */
val kafkaModule: Module = module {
    single<AppKafkaConsumer> {
        AppKafkaConsumer(
            config = get(),
            loggerProvider = get()
        )
    }

    single<AppKafkaProducer> {
        AppKafkaProducer(
            config = get(),
            loggerProvider = get()
        )
    }

    single<AppKafkaController> {
        AppKafkaController(
            config = get(),
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
    kafkaModule
)
