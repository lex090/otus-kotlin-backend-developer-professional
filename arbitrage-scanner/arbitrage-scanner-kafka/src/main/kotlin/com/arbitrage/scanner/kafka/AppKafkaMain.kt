package com.arbitrage.scanner.kafka

import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import kotlin.system.exitProcess

/**
 * Главная точка входа в Kafka приложение.
 * Инициализирует Koin DI контейнер и запускает AppKafkaController.
 */
fun main() {
    // Инициализация Koin DI
    val koinApp = startKoin {
        // Логирование Koin
        logger(PrintLogger(Level.INFO))

        // Загрузка модулей
        modules(allModules)
    }

    val controller = koinApp.koin.get<AppKafkaController>()

    // Обработка сигнала завершения для graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Получен сигнал завершения приложения...")
        controller.close()
        stopKoin()
        println("Приложение остановлено")
    })

    try {
        println("Запуск Kafka приложения...")
        println("Для остановки нажмите Ctrl+C")

        // Блокирующий запуск контроллера
        runBlocking {
            controller.start()
        }
    } catch (e: Exception) {
        println("Ошибка при запуске приложения: ${e.message}")
        e.printStackTrace()
        controller.close()
        stopKoin()
        exitProcess(1)
    }
}
