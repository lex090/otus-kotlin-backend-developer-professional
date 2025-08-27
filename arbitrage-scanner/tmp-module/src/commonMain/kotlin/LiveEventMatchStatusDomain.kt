class LiveEventMatchStatusDomain private constructor(
    private val liveEventStatus: LiveEventStatus,
    private val liveEventTimerDomain: LiveEventTimerDomain?,
    private val matchStatusName: String?,
) {

    fun apply(currentSystemTimestamp: Long): LiveEventMatchStatusValue {
        val timerValue = liveEventTimerDomain?.apply(currentSystemTimestamp = currentSystemTimestamp)
        return when {
            liveEventStatus == LiveEventStatus.Stopped
                    || liveEventStatus == LiveEventStatus.Final -> {
                LiveEventMatchStatusValue.Finish
            }

            matchStatusName.isNullOrBlank() -> {
                LiveEventMatchStatusValue.NoInfo
            }

            timerValue == null -> {
                LiveEventMatchStatusValue.StatusValue(timeName = matchStatusName)
            }

            else -> {
                LiveEventMatchStatusValue.StatusValueWithTimer(
                    matchStatusName = matchStatusName,
                    liveEventTimerValue = timerValue,
                )
            }
        }
    }

    sealed interface LiveEventMatchStatusValue {
        data object NoInfo : LiveEventMatchStatusValue

        data object Finish : LiveEventMatchStatusValue

        data class StatusValue(
            val timeName: String,
        ) : LiveEventMatchStatusValue

        data class StatusValueWithTimer(
            val matchStatusName: String,
            val liveEventTimerValue: LiveEventTimerDomain.LiveEventTimerValue,
        ) : LiveEventMatchStatusValue
    }

    companion object {
        fun create(
            liveEventStatus: LiveEventStatus,
            liveEventTimerDomain: LiveEventTimerDomain?,
            matchStatusName: String?,
        ): LiveEventMatchStatusDomain {
            return LiveEventMatchStatusDomain(
                liveEventStatus = liveEventStatus,
                liveEventTimerDomain = liveEventTimerDomain,
                matchStatusName = matchStatusName,
            )
        }
    }
}
