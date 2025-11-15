package com.arbitrage.scanner.models

data class RecalculateResult(
    val opportunitiesCount: Int,
    val processingTimeMs: Long,
) {
    companion object {
        val NONE = RecalculateResult(
            opportunitiesCount = -1,
            processingTimeMs = -1L,
        )
    }
}
