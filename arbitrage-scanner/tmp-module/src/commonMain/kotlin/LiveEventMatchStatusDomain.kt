/**
 * Доменная модель для управления статусом матча с учетом таймера.
 * 
 * Объединяет статус события, таймер и название статуса для формирования
 * итогового отображения информации о статусе события.
 * 
 * @property liveEventStatus текущий статус события
 * @property liveEventTimerDomain домен для работы с таймером события (опционально)
 * @property matchStatusName текстовое название статуса события (опционально)
 */
class LiveEventMatchStatusDomain private constructor(
    private val liveEventStatus: LiveEventStatus,
    private val liveEventTimerDomain: LiveEventTimerDomain?,
    private val matchStatusName: String?,
) {

    /**
     * Применяет бизнес-логику для определения итогового значения статуса события.
     * 
     * @param currentSystemTimestamp текущая системная временная метка для расчета таймера
     * @return значение статуса события с учетом всех условий
     */
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

    /**
     * Представляет возможные варианты отображения статуса события.
     */
    sealed interface LiveEventMatchStatusValue {
        /** Нет информации о статусе события */
        data object NoInfo : LiveEventMatchStatusValue

        /** Событие завершено */
        data object Finish : LiveEventMatchStatusValue

        /**
         * Статус события только с названием.
         * 
         * @property timeName название статуса для отображения
         */
        data class StatusValue(
            val timeName: String,
        ) : LiveEventMatchStatusValue

        /**
         * Статус события с названием и таймером.
         * 
         * @property matchStatusName название статуса события
         * @property liveEventTimerValue значение таймера события
         */
        data class StatusValueWithTimer(
            val matchStatusName: String,
            val liveEventTimerValue: LiveEventTimerDomain.LiveEventTimerValue,
        ) : LiveEventMatchStatusValue
    }

    companion object {
        /**
         * Создает экземпляр [LiveEventMatchStatusDomain] с заданными параметрами.
         * 
         * @param liveEventStatus статус события
         * @param liveEventTimerDomain домен для работы с таймером (может быть null)
         * @param matchStatusName название статуса события (может быть null)
         * @return новый экземпляр доменной модели
         */
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
