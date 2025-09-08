package com.arbitrage.scanner

import com.arbitrage.scanner.context.Context

fun interface BusinessLogicProcessor {
    suspend fun exec(ctx: Context)
}
