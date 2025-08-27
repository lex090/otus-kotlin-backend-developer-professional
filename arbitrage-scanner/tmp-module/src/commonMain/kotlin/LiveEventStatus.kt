/**
 * Представляет состояние события.
 * 
 * Используется для определения текущего статуса события и влияет на отображение
 * информации о событии и доступные действия.
 */
sealed interface LiveEventStatus {
    /** Событие активно и продолжается */
    data object Active : LiveEventStatus
    
    /** Событие приостановлено (временная остановка) */
    data object Paused : LiveEventStatus
    
    /** Перерыв в событии */
    data object Break : LiveEventStatus
    
    /** Событие остановлено */
    data object Stopped : LiveEventStatus
    
    /** Событие завершено окончательно */
    data object Final : LiveEventStatus
    
    /** Только дополнительное исходы */
    data object OnlyExtra : LiveEventStatus
    
    /** Неизвестное состояние события */
    data object Unknown : LiveEventStatus

    companion object {
        /**
         * Создает экземпляр [LiveEventStatus] на основе числового кода статуса.
         * 
         * @param eventStatus числовой код статуса события, может быть null
         * @return соответствующий статус события или [Unknown] для неизвестных кодов
         */
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
