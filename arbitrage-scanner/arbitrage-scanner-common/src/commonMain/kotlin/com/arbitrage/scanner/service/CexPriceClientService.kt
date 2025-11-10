package com.arbitrage.scanner.service

import com.arbitrage.scanner.models.CexPrice

/**
 * Компонент для получения цен с CEX из нашего внутреннего микросервиса.
 * Упрощенный вид интерфейса.
 */
interface CexPriceClientService {
    suspend fun getAllCexPrice(): Set<CexPrice>

    companion object {
        val NONE = object : CexPriceClientService {
            override suspend fun getAllCexPrice(): Set<CexPrice> {
                throw NotImplementedError("CexPriceClientService.NONE must not be used")
            }
        }
    }
}
