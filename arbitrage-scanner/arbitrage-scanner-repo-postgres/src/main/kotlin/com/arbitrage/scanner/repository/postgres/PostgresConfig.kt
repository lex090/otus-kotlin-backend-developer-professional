package com.arbitrage.scanner.repository.postgres

/**
 * Конфигурация подключения к PostgreSQL.
 *
 * Использует environment variables с fallback на default значения для local development.
 */
data class PostgresConfig(
    val host: String = System.getenv("POSTGRES_HOST") ?: "localhost",
    val port: Int = System.getenv("POSTGRES_PORT")?.toIntOrNull() ?: 5432,
    val database: String = System.getenv("POSTGRES_DB") ?: "arbitrage",
    val user: String = System.getenv("POSTGRES_USER") ?: "postgres",
    val password: String = System.getenv("POSTGRES_PASSWORD") ?: "postgres",
    val schema: String = System.getenv("POSTGRES_SCHEMA") ?: "public",
    val maxPoolSize: Int = System.getenv("POSTGRES_MAX_POOL_SIZE")?.toIntOrNull() ?: 20,
    val minIdle: Int = System.getenv("POSTGRES_MIN_IDLE")?.toIntOrNull() ?: 5,
    val connectionTimeout: Long = 30_000L,  // 30 seconds
    val idleTimeout: Long = 600_000L,       // 10 minutes
    val maxLifetime: Long = 1_800_000L      // 30 minutes
)
