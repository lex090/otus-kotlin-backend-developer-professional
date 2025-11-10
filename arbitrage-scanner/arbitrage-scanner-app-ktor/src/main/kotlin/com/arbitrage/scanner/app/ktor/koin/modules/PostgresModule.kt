package com.arbitrage.scanner.app.ktor.koin.modules

import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.postgres.DatabaseFactory
import com.arbitrage.scanner.repository.postgres.PostgresArbOpRepository
import com.arbitrage.scanner.repository.postgres.PostgresConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin модуль для конфигурации PostgreSQL репозитория.
 *
 * Конфигурация читается из переменных окружения:
 * - POSTGRES_HOST (по умолчанию: localhost)
 * - POSTGRES_PORT (по умолчанию: 5432)
 * - POSTGRES_DB (по умолчанию: arbitrage)
 * - POSTGRES_USER (по умолчанию: postgres)
 * - POSTGRES_PASSWORD (по умолчанию: postgres)
 */
val postgresModule = module {
    // PostgreSQL репозиторий с именем "postgres" для dependency injection
    // Database создается внутри и не экспортируется наружу
    single<IArbOpRepository>(named("postgres")) {
        val config = PostgresConfig(
            host = System.getenv("POSTGRES_HOST") ?: "localhost",
            port = System.getenv("POSTGRES_PORT")?.toIntOrNull() ?: 5432,
            database = System.getenv("POSTGRES_DB") ?: "arbitrage",
            user = System.getenv("POSTGRES_USER") ?: "postgres",
            password = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
        )
        val database = DatabaseFactory.init(config)
        PostgresArbOpRepository(database)
    }
}
