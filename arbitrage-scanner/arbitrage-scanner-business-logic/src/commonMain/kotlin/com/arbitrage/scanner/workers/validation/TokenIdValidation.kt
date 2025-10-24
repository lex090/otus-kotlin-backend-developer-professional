package com.arbitrage.scanner.workers.validation

/**
 * Проверка валидности ID токена
 */
internal fun isValidTokenId(id: String): Boolean {
    // Пустая строка допустима для пустых фильтров
    if (id.isEmpty()) return true

    // Проверка длины
    if (id.length !in MIN_TOKEN_ID_LENGTH..MAX_TOKEN_ID_LENGTH) return false

    // Проверка формата: буквы, цифры и дефисы, не может начинаться или заканчиваться дефисом
    val tokenIdRegex = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$")

    return when (id.length) {
        1 -> false // Токены обычно имеют минимум 2 символа
        2 -> id.matches(Regex("^[a-zA-Z0-9]{2}$"))
        else -> id.matches(tokenIdRegex)
    }
}

internal const val MIN_TOKEN_ID_LENGTH = 2
internal const val MAX_TOKEN_ID_LENGTH = 50
