package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp

sealed class ArbitrageOpportunity {
    abstract val id: ArbitrageOpportunityId
    abstract val startTimestamp: Timestamp
    abstract val endTimestamp: Timestamp?

    val type: ArbitrageOpportunityType by lazy {
        when (this) {
            is DexToCexSimpleArbitrageOpportunity -> ArbitrageOpportunityType.DEX_TO_CEX_SIMPLE
        }
    }

    val status: ArbitrageOpportunityStatus by lazy {
        when {
            endTimestamp == null -> ArbitrageOpportunityStatus.ACTIVE
            else -> ArbitrageOpportunityStatus.EXPIRED
        }
    }

    data class DexToCexSimpleArbitrageOpportunity(
        override val id: ArbitrageOpportunityId,
        override val startTimestamp: Timestamp,
        override val endTimestamp: Timestamp?,
        val dexPrice: DexPrice,
        val cexPrice: CexPrice,
        val spread: ArbitrageOpportunitySpread,
    ) : ArbitrageOpportunity() {

        companion object {
            val DEFAULT = DexToCexSimpleArbitrageOpportunity(
                id = ArbitrageOpportunityId.DEFAULT,
                dexPrice = DexPrice.DEFAULT,
                cexPrice = CexPrice.DEFAULT,
                spread = ArbitrageOpportunitySpread.DEFAULT,
                startTimestamp = Timestamp.DEFAULT,
                endTimestamp = Timestamp.DEFAULT,
            )
        }
    }
}
