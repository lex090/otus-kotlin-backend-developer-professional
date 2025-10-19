package com.arbitrage.scanner.models

data class RecalculateResult(
    val opportunitiesCount: Int,
    val processingTimeMs: Long,
) {
    companion object {
        val DEFAULT = RecalculateResult(
            opportunitiesCount = 0,
            processingTimeMs = 0L,
        )
    }
}
