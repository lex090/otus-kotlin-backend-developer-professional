package com.arbitrage.scanner.models

sealed class ArbitrageOpportunity {
    abstract val id: ArbitrageOpportunityId
    abstract val startTimeStamp: TimeStamp
    abstract val endTimeStamp: TimeStamp?

    fun type(): ArbitrageOpportunityType {
        return when (this) {
            is DexToCexSimpleArbitrageOpportunity -> ArbitrageOpportunityType.DEX_TO_CEX_SIMPLE
        }
    }

    fun isActive(): ArbitrageOpportunityStatus =
        when {
            endTimeStamp == null -> ArbitrageOpportunityStatus.ACTIVE
            else -> ArbitrageOpportunityStatus.EXPIRED
        }

    class DexToCexSimpleArbitrageOpportunity(
        override val id: ArbitrageOpportunityId,
        override val startTimeStamp: TimeStamp,
        override val endTimeStamp: TimeStamp?,
        val dexPrice: DexPrice,
        val cexPrice: CexPrice,
        val spread: ArbitrageOpportunitySpread,
    ) : ArbitrageOpportunity() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DexToCexSimpleArbitrageOpportunity

            if (id != other.id) return false
            if (startTimeStamp != other.startTimeStamp) return false
            if (endTimeStamp != other.endTimeStamp) return false
            if (dexPrice != other.dexPrice) return false
            if (cexPrice != other.cexPrice) return false
            if (spread != other.spread) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + startTimeStamp.hashCode()
            result = 31 * result + (endTimeStamp?.hashCode() ?: 0)
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
                startTimeStamp = TimeStamp.DEFAULT,
                endTimeStamp = TimeStamp.DEFAULT,
            )
        }
    }
}
