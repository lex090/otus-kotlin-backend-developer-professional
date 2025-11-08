package com.arbitrage.scanner.repository.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

/**
 * Factory для инициализации подключения к PostgreSQL.
 *
 * **ВАЖНО**:
 * - Отвечает ТОЛЬКО за создание connection pool (HikariCP)
 * - НЕ запускает Liquibase миграции
 * - НЕ создает схему через SchemaUtils
 *
 * Схема БД должна быть создана заранее через Liquibase миграции
 * (запускаются через Docker init-container или Liquibase CLI).
 */
object DatabaseFactory {

    /**
     * Инициализирует connection pool к PostgreSQL и подключает Exposed.
     *
     * @param config Конфигурация подключения
     * @return Database instance для использования в queries
     */
    fun init(config: PostgresConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
            driverClassName = "org.postgresql.Driver"
            username = config.user
            password = config.password
            schema = config.schema

            // Connection pool settings
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minIdle
            connectionTimeout = config.connectionTimeout
            idleTimeout = config.idleTimeout
            maxLifetime = config.maxLifetime

            // Health check
            connectionTestQuery = "SELECT 1"
        }

        val dataSource = HikariDataSource(hikariConfig)

        return Database.connect(dataSource)
    }
}
