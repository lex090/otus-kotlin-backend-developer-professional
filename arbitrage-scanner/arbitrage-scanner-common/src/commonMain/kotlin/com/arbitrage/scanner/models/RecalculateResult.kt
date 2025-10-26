package com.arbitrage.scanner.models

data class RecalculateResult(
    val opportunitiesCount: Int = 0,
    val processingTimeMs: Long = 0L,
) {
    companion object {
        val DEFAULT = RecalculateResult()
    }
}
