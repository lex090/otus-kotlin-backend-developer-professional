package com.arbitrage.scanner.workers.validation

/**
 * Общая функция валидации ID биржи.
 *
 * Правила валидации:
 * - ID не должен быть пустым (если указан)
 * - Минимальная длина: 3 символа (например, "OKX")
 * - Максимальная длина: 50 символов
 * - Допустимые символы: буквы (a-z, A-Z), цифры (0-9) и дефисы (-)
 *
 * Примеры корректных ID:
 * - DEX: pancakeswap, uniswap-v3, sushiswap, 1inch
 * - CEX: binance, okx, bybit, kraken, coinbase
 */
internal fun isValidExchangeId(id: String): Boolean {
    // Пустая строка допустима для пустых фильтров
    if (id.isEmpty()) return true

    // Проверка длины
    if (id.length !in MIN_EXCHANGE_ID_LENGTH..MAX_EXCHANGE_ID_LENGTH) return false

    // Проверка формата: буквы, цифры и дефисы
    // Для бирж допускаем начало с цифры (например, "1inch")
    val exchangeIdRegex = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$")
    val shortExchangeRegex = Regex("^[a-zA-Z0-9]{3}$")

    return when (id.length) {
        3 -> id.matches(shortExchangeRegex)
        else -> id.matches(exchangeIdRegex)
    }
}

private const val MIN_EXCHANGE_ID_LENGTH = 3
private const val MAX_EXCHANGE_ID_LENGTH = 50
