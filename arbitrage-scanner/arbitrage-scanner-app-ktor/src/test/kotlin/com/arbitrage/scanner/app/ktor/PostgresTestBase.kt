package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.repository.postgres.ArbitrageOpportunitiesTable
import com.arbitrage.scanner.repository.postgres.DatabaseFactory
import com.arbitrage.scanner.repository.postgres.PostgresConfig
import kotlinx.coroutines.Dispatchers
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import liquibase.database.DatabaseFactory as LiquibaseDatabaseFactory

/**
 * Singleton для тестов Ktor приложения с PostgreSQL репозиторием.
 *
 * Использует Testcontainers для запуска реального PostgreSQL в Docker.
 * Выполняет Liquibase миграции перед тестами.
 */
@Testcontainers
object PostgresTestBase {

    @Container
    @JvmField
    val postgres: PostgreSQLContainer = PostgreSQLContainer(
        DockerImageName.parse("postgres:15-alpine")
    )
        .withDatabaseName("arbitrage_test")
        .withUsername("test")
        .withPassword("test")
        .apply {
            start()  // Запускаем контейнер сразу
        }

    val database: Database

    init {
        // Создаем PostgresConfig из Testcontainers параметров
        val config = PostgresConfig(
            host = postgres.host,
            port = postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            database = postgres.databaseName,
            user = postgres.username,
            password = postgres.password
        )

        // Инициализируем connection pool
        database = DatabaseFactory.init(config)

        // Запускаем Liquibase миграции
        val connection = postgres.createConnection("")
        val liquibaseDatabase = LiquibaseDatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        Liquibase(
            "db/changelog/db.changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            liquibaseDatabase
        ).update("")

        connection.close()
    }

    /**
     * Очищает все данные из таблицы arbitrage_opportunities.
     * Используется в @BeforeEach для обеспечения изоляции тестов.
     */
    suspend fun clearDatabase() {
        newSuspendedTransaction(Dispatchers.IO, database) {
            ArbitrageOpportunitiesTable.deleteAll()
        }
    }
}
