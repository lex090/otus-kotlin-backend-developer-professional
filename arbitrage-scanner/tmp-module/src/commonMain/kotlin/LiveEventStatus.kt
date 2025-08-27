sealed interface LiveEventStatus {
    data object Active : LiveEventStatus
    data object Paused : LiveEventStatus
    data object Break : LiveEventStatus
    data object Stopped : LiveEventStatus
    data object Final : LiveEventStatus
    data object OnlyExtra : LiveEventStatus
    data object Unknown : LiveEventStatus

    companion object {
        fun create(eventStatus: Int?): LiveEventStatus {
            return when (eventStatus) {
                0 -> Active
                1 -> Paused
                2 -> Break
                3 -> Stopped
                4 -> Final
                5 -> OnlyExtra
                else -> Unknown
            }
        }
    }
}
