package com.arbitrage.scanner.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

/**
 * Загрузчик конфигурации приложения
 *
 * Использует библиотеку Hoplite для загрузки конфигурации из YAML файла
 * application.yaml, расположенного в ресурсах приложения.
 */
object ConfigLoader {

    /**
     * Загружает конфигурацию приложения из файла application.yaml
     *
     * @return [AppConfig] с загруженными настройками
     * @throws Exception если файл конфигурации не найден или содержит ошибки
     */
    fun loadConfig(): AppConfig {
        return ConfigLoaderBuilder.default()
            .addResourceSource("/application.yaml")
            .build()
            .loadConfigOrThrow<AppConfig>()
    }
}