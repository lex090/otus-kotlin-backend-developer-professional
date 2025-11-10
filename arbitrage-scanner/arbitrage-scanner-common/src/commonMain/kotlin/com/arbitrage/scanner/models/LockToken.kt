package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

/**
 * Токен для оптимистичной блокировки (Optimistic Locking).
 *
 * При каждом обновлении записи генерируется новый UUID токен.
 * UPDATE операция должна проверять, что lockToken не изменился с момента чтения.
 */
@JvmInline
value class LockToken(val value: String = "") {

    fun isDefault(): Boolean = this == DEFAULT

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val DEFAULT = LockToken()
    }
}
