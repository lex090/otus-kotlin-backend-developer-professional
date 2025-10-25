package com.arbitrage.scanner.app.kafka

import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.system.exitProcess

/**
 * Главная точка входа в Kafka приложение.
 * Инициализирует Koin DI контейнер и запускает AppKafkaController.
 */
suspend fun main() {
    // Инициализация Koin DI
    val koinApp = startKoin {
        modules(allModules)
    }

    // Получение зависимостей из Koin
    val loggerProvider = koinApp.koin.get<ArbScanLoggerProvider>()
    val logger = loggerProvider.logger("AppKafkaMain")
    val controller = koinApp.koin.get<AppKafkaController>()

    // Обработка сигнала завершения для graceful shutdown
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Получен сигнал завершения приложения...")
            controller.close()
            stopKoin()
            logger.info("Приложение остановлено")
        }
    )

    try {
        logger.info("Запуск Kafka приложения...")
        controller.start()
    } catch (e: Exception) {
        logger.error(msg = "Ошибка при запуске приложения", e = e)
        exitProcess(1)
    }
}
