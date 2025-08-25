/**
 * Доменная модель таймера.
 *
 * @param isShow - Нужно ли отображать таймер
 * @param secondsFromEventStart - Количество секунд с начала старта события
 * @param secondsFromEventStartMd - Временная метка обновления secondsFromEventStart
 * @param isRunning - Запущен таймер или нет.
 * @param format - "mm:ss" // TODO тут пока вопрос как обрабатывать так как LocalTime не подходит.
 *
 *  Пример данных которые приходят с бека.
 *  ```
 *   "is_show": 1,
 *   "tmr": 4200,
 *   "tmr_md": 1749824561,
 *   "is_run": 1,
 *   "format": "mm:ss"
 *   ```
 */
class LiveTimerDomain private constructor(
    private val isShow: Boolean,
    private val secondsFromEventStart: Long,
    private val secondsFromEventStartMd: Long,
    private val isRunning: Boolean,
    private val format: String,
) {

    /**
     * Валидация на уровне создания класса.
     * Проверяем валидность данных на возможную дальнейшую работу
     */
    init {
        require(secondsFromEventStart >= 0) {
            "timerValueInSeconds -> $secondsFromEventStart должен быть больше или равен 0 "
        }

        require(secondsFromEventStartMd > 0) {
            "secondsFromEventStartMd -> $secondsFromEventStartMd должен быть больше 0 "
        }

        require(format.isNotBlank()) { "Формат не должен быть пустым" }
    }

    /**
     * Применить текущее время системы к таймеру и получить конечное значение.
     * Если вдруг у нас происходит какая-то ошибка при расчете таймера,
     * либо таймер не должен в будущем отображаться, то мы возвращаем null
     */
    fun apply(currentSystemTimestamp: Long): LiveTimerValue? {
        if (!isShow) {
            return null
        }

        if (isRunning) {
            return calculateIsRunningTimer(currentSystemTimestamp = currentSystemTimestamp)
        }

        return calculateDefaultTimer()
    }

    private fun calculateIsRunningTimer(currentSystemTimestamp: Long): LiveTimerValue? {
        return runCatching {
            val secondsFromEventStart = calculateActualTimestamp(currentSystemTimestamp = currentSystemTimestamp)

            LiveTimerValue(
                isRunning = true,
                totalSecondsFromEventStart = secondsFromEventStart,
                format = format,
            )
        }.getOrNull()
    }

    private fun calculateActualTimestamp(currentSystemTimestamp: Long): Long {
        // Время, которое прошло с момента обновления данных до текущего времени в приложении
        val deltaTime = currentSystemTimestamp - secondsFromEventStartMd
        return secondsFromEventStart + deltaTime
    }

    private fun calculateDefaultTimer(): LiveTimerValue {
        return LiveTimerValue(
            isRunning = false,
            totalSecondsFromEventStart = secondsFromEventStart,
            format = format,
        )
    }

    /**
     * Класс хранящий в себе результирующую информацию после применения времени системы к таймеру.
     */
    data class LiveTimerValue(
        val isRunning: Boolean,
        val totalSecondsFromEventStart: Long,
        val format: String,
    )

    companion object {
        /**
         * Запрещаем создавать класс напрямую.
         * Используем только метод.
         *
         * @param onInstanceCreationFailedCallback - Используем для логирования если валидация
         *  модели при создании упала с ошибкой
         */
        fun createOrNull(
            isShow: Int?,
            timerValueInSeconds: Long?,
            timerValueInSecondsMd: Long?,
            isRunning: Int?,
            format: String?,
            onInstanceCreationFailedCallback: ((Throwable) -> Unit)? = null,
        ): LiveTimerDomain? {
            return runCatching {
                validateParamsAndCreateInstance(
                    isShow = isShow,
                    timerValueInSeconds = timerValueInSeconds,
                    timerValueInSecondsMd = timerValueInSecondsMd,
                    isRunning = isRunning,
                    format = format
                )
            }.onFailure { throwable -> onInstanceCreationFailedCallback?.invoke(throwable) }
                .getOrDefault(null)
        }

        /**
         * Базовая валидация значений с бекенда.
         * Суть метода предотвратить создание класса с не корректными по семантике типам.
         * Тут не происходит валидации на смысловые значения внутри переменных.
         */
        private fun validateParamsAndCreateInstance(
            isShow: Int?,
            timerValueInSeconds: Long?,
            timerValueInSecondsMd: Long?,
            isRunning: Int?,
            format: String?,
        ): LiveTimerDomain {
            requireNotNull(timerValueInSeconds) { "timerValueInSeconds не должен быть равен null" }
            requireNotNull(timerValueInSecondsMd) {
                "timerValueInSecondsMd не должен быть равен null"
            }
            requireNotNull(format) { "format не должен быть равен null" }

            return LiveTimerDomain(
                isShow = when (isShow) {
                    1 -> true
                    0 -> false
                    else -> error("Невалидное значение параметра isShow = $isShow")
                },
                secondsFromEventStart = timerValueInSeconds,
                secondsFromEventStartMd = timerValueInSecondsMd,
                isRunning = when (isRunning) {
                    1 -> true
                    0 -> false
                    else -> error("Невалидное значение параметра isRunning = $isRunning")
                },
                format = format,
            )
        }
    }
}
