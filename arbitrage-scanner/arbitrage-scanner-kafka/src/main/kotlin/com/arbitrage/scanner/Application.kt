package com.arbitrage.scanner

import com.arbitrage.scanner.consumers.ArbitrageKafkaConsumer
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.java.KoinJavaComponent.inject
import kotlin.system.exitProcess

/**
 * Точка входа в Kafka транспорт приложения Arbitrage Scanner
 *
 * Инициализирует:
 * - Koin DI контейнер
 * - Kafka consumer для обработки запросов
 * - Graceful shutdown через shutdown hook
 */
fun main() = runBlocking {
    // Инициализация Koin
    startKoin {
        modules(allModules)
    }

    val loggerProvider by inject<ArbScanLoggerProvider>(ArbScanLoggerProvider::class.java)
    val logger = loggerProvider.logger("Application")
    val consumer by inject<ArbitrageKafkaConsumer>(ArbitrageKafkaConsumer::class.java)

    logger.info(msg = "Starting Arbitrage Scanner Kafka Transport", marker = "APP")

    // Регистрация shutdown hook для graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            logger.info(msg = "Shutting down Arbitrage Scanner", marker = "APP")
            try {
                val responseProducer by inject<ResponseProducer>(ResponseProducer::class.java)
                consumer.stop()
                responseProducer.close()
                stopKoin()
                logger.info(msg = "Shutdown completed", marker = "APP")
            } catch (e: Exception) {
                logger.error(msg = "Error during shutdown", marker = "APP", e = e)
                exitProcess(1)
            }
        }
    })

    try {
        // Запуск consumer
        consumer.start(this)

        logger.info(msg = "Arbitrage Scanner Kafka Transport started successfully", marker = "APP")

        // Ожидание завершения (consumer будет работать пока не получит сигнал завершения)
        Thread.currentThread().join()

    } catch (e: Exception) {
        logger.error(msg = "Fatal error in application", marker = "APP", e = e)
        exitProcess(1)
    }
}