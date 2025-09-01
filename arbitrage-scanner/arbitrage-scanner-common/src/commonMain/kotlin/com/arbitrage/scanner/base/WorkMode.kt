package com.arbitrage.scanner.base

sealed interface WorkMode {
    data object PROD : WorkMode
    data object TEST : WorkMode
    data object STUB : WorkMode
}
