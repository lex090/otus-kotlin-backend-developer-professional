package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp

sealed class ArbitrageOpportunity {
    abstract val id: ArbitrageOpportunityId
    abstract val startTimestamp: Timestamp
    abstract val endTimestamp: Timestamp?

    fun type(): ArbitrageOpportunityType {
        return when (this) {
            is DexToCexSimpleArbitrageOpportunity -> ArbitrageOpportunityType.DEX_TO_CEX_SIMPLE
        }
    }

    fun isActive(): ArbitrageOpportunityStatus =
        when {
            endTimestamp == null -> ArbitrageOpportunityStatus.ACTIVE
            else -> ArbitrageOpportunityStatus.EXPIRED
        }

    class DexToCexSimpleArbitrageOpportunity private constructor(
        override val id: ArbitrageOpportunityId,
        override val startTimestamp: Timestamp,
        override val endTimestamp: Timestamp?,
        val dexPrice: DexPrice,
        val cexPrice: CexPrice,
        val spread: ArbitrageOpportunitySpread,
    ) : ArbitrageOpportunity() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DexToCexSimpleArbitrageOpportunity

            if (id != other.id) return false
            if (startTimestamp != other.startTimestamp) return false
            if (endTimestamp != other.endTimestamp) return false
            if (dexPrice != other.dexPrice) return false
            if (cexPrice != other.cexPrice) return false
            if (spread != other.spread) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + startTimestamp.hashCode()
            result = 31 * result + (endTimestamp?.hashCode() ?: 0)
            result = 31 * result + dexPrice.hashCode()
            result = 31 * result + cexPrice.hashCode()
            result = 31 * result + spread.hashCode()
            return result
        }

        companion object {
            val DEFAULT = DexToCexSimpleArbitrageOpportunity(
                id = ArbitrageOpportunityId.DEFAULT,
                dexPrice = DexPrice.DEFAULT,
                cexPrice = CexPrice.DEFAULT,
                spread = ArbitrageOpportunitySpread.DEFAULT,
                startTimestamp = Timestamp.DEFAULT,
                endTimestamp = Timestamp.DEFAULT,
            )

            fun create(
                id: ArbitrageOpportunityId,
                startTimestamp: Timestamp,
                endTimestamp: Timestamp?,
                dexPrice: DexPrice,
                cexPrice: CexPrice,
                spread: ArbitrageOpportunitySpread,
            ): DexToCexSimpleArbitrageOpportunity {
                return DexToCexSimpleArbitrageOpportunity(
                    id = id,
                    startTimestamp = startTimestamp,
                    endTimestamp = endTimestamp,
                    dexPrice = dexPrice,
                    cexPrice = cexPrice,
                    spread = spread,
                )
            }
        }
    }
}
