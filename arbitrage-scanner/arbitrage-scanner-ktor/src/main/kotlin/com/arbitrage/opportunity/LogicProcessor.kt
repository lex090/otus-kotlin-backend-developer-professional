package com.arbitrage.opportunity

import com.arbitrage.scanner.context.Context

fun interface LogicProcessor {
    suspend fun exec(ctx: Context)
}
