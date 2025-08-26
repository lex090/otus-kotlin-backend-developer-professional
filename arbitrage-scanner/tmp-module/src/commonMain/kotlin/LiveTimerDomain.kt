/**
 * Доменная модель live-таймера спортивного события.
 *
 * Инкапсулирует логику расчета текущего времени таймера с учетом системного времени
 * и защищает от некорректных данных (переполнение, отрицательные значения).
 *
 * @param isShow Флаг отображения таймера в пользовательском интерфейсе
 * @param secondsFromEventStart Базовое время таймера в секундах на момент последнего обновления
 * @param secondsFromEventStartMd Unix timestamp (в секундах) момента последнего обновления данных с бэкенда
 * @param isRunning Флаг активности таймера (true - тикает в реальном времени, false - остановлен)
 * @param format Строка формата отображения времени (например, "mm:ss", "hh:mm:ss")
 *
 * ### Пример JSON данных с бэкенда:
 * ```json
 * {
 *   "is_show": 1,
 *   "tmr": 4200,
 *   "tmr_md": 1749824561,
 *   "is_run": 1,
 *   "format": "mm:ss"
 * }
 * ```
 *
 * ### Использование:
 * ```kotlin
 * val timer = LiveTimerDomain.createOrNull(
 *     isShow = 1,
 *     timerValueInSeconds = 4200L,
 *     timerValueInSecondsMd = 1749824561L,
 *     isRunning = 1,
 *     format = "mm:ss"
 * )
 *
 * val currentTime = System.currentTimeMillis() / 1000
 * val result = timer?.apply(currentTime)
 * ```
 */
class LiveTimerDomain private constructor(
    private val isShow: Boolean,
    private val secondsFromEventStart: Long,
    private val secondsFromEventStartMd: Long,
    private val isRunning: Boolean,
    private val format: String,
) {

    /**
     * Валидация инвариантов доменной модели.
     *
     * Проверяет корректность входных данных и предотвращает создание объекта
     * с некорректными значениями, которые могут привести к ошибкам во время выполнения.
     * Применяет принцип "fail-fast" для раннего обнаружения проблем.
     *
     * @throws IllegalArgumentException если данные не соответствуют бизнес-правилам
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
     * Вычисляет актуальное состояние таймера на основе текущего системного времени.
     *
     * Для активного таймера (isRunning=true) рассчитывает прошедшее время с момента
     * последнего обновления и прибавляет его к базовому значению. Для остановленного
     * таймера возвращает статичное значение.
     *
     * @param currentSystemTimestamp Unix timestamp текущего системного времени в секундах
     * @return [LiveTimerValue] с актуальными данными или null в следующих случаях:
     *   - Таймер не должен отображаться (isShow=false)
     *   - Системное время меньше времени последнего обновления (время "откатилось назад")
     *   - Произошло переполнение Long при вычислении (результат стал отрицательным)
     *
     * ### Пример использования:
     * ```kotlin
     * val currentTime = System.currentTimeMillis() / 1000
     * val timerState = timer.apply(currentTime)
     * if (timerState != null) {
     *     println("Время: ${timerState.totalSecondsFromEventStart}s, формат: ${timerState.format}")
     * }
     * ```
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
        val secondsFromEventStart =
            calculateActualTimestamp(currentSystemTimestamp = currentSystemTimestamp) ?: return null

        return LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = secondsFromEventStart,
        )
    }

    /**
     * Вычисляет актуальное время таймера с учетом прошедшего системного времени.
     *
     * Алгоритм работы:
     * 1. Рассчитывает дельту времени между текущим моментом и последним обновлением
     * 2. Прибавляет дельту к базовому значению таймера
     * 3. Проверяет корректность результата (защита от переполнения и некорректного времени)
     *
     * @param currentSystemTimestamp Unix timestamp текущего системного времени в секундах
     * @return Актуальное время таймера в секундах или null при ошибке вычисления
     */
    private fun calculateActualTimestamp(currentSystemTimestamp: Long): Long? {
        // Защита от "отката" системного времени назад
        if (currentSystemTimestamp < secondsFromEventStartMd) {
            return null
        }

        // Время, прошедшее с момента последнего обновления данных
        val deltaTime = currentSystemTimestamp - secondsFromEventStartMd

        val resultTotalSeconds = secondsFromEventStart + deltaTime

        // Защита от переполнения Long (результат становится отрицательным)
        if (resultTotalSeconds < 0) {
            return null
        }

        return resultTotalSeconds
    }

    private fun calculateDefaultTimer(): LiveTimerValue {
        return LiveTimerValue(
            isRunning = false,
            totalSecondsFromEventStart = secondsFromEventStart,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LiveTimerDomain

        if (isShow != other.isShow) return false
        if (secondsFromEventStart != other.secondsFromEventStart) return false
        if (secondsFromEventStartMd != other.secondsFromEventStartMd) return false
        if (isRunning != other.isRunning) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isShow.hashCode()
        result = 31 * result + secondsFromEventStart.hashCode()
        result = 31 * result + secondsFromEventStartMd.hashCode()
        result = 31 * result + isRunning.hashCode()
        result = 31 * result + format.hashCode()
        return result
    }

    /**
     * Value объект, представляющий снимок состояния live-таймера в конкретный момент времени.
     *
     * Содержит все необходимые данные для отображения таймера в UI слое.
     * Является immutable и может безопасно передаваться между слоями приложения
     * без риска случайных изменений состояния.
     *
     * @property isRunning Флаг активности таймера (true - тикает в реальном времени, false - остановлен)
     * @property totalSecondsFromEventStart Общее время от начала события в секундах
     */
    class LiveTimerValue(
        val isRunning: Boolean,
        private val totalSecondsFromEventStart: Long,
    ) {

        /**
         * Базовое форматирование таймера вида mm:ss.
         */
        fun simpleFormat(): String {
            val mm = (totalSecondsFromEventStart / 60).toString().padStart(2, '0')
            val ss = (totalSecondsFromEventStart % 60).toString().padStart(2, '0')
            return "$mm:$ss"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LiveTimerValue

            if (isRunning != other.isRunning) return false
            if (totalSecondsFromEventStart != other.totalSecondsFromEventStart) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isRunning.hashCode()
            result = 31 * result + totalSecondsFromEventStart.hashCode()
            return result
        }
    }

    companion object {
        /**
         * Factory method для безопасного создания экземпляра LiveTimerDomain.
         *
         * Преобразует сырые данные с бэкенда в типизированную доменную модель
         * с полной валидацией входных параметров. Использует fail-safe подход:
         * при любой ошибке возвращает null вместо выброса исключения, что предотвращает
         * падение приложения из-за некорректных данных.
         *
         * @param isShow Флаг отображения таймера (0 - скрыт, 1 - показывать)
         * @param timerValueInSeconds Базовое время таймера в секундах с начала события
         * @param timerValueInSecondsMd Unix timestamp (в секундах) момента последнего обновления
         * @param isRunning Флаг активности таймера (0 - остановлен, 1 - активен)
         * @param format Строка формата отображения времени
         * @param onInstanceCreationFailedCallback Callback для обработки ошибок валидации (опционально)
         * @return Экземпляр [LiveTimerDomain] или null при некорректных данных
         *
         * ### Пример использования:
         * ```kotlin
         * val timer = LiveTimerDomain.createOrNull(
         *     isShow = jsonObject.getInt("is_show"),
         *     timerValueInSeconds = jsonObject.getLong("tmr"),
         *     timerValueInSecondsMd = jsonObject.getLong("tmr_md"),
         *     isRunning = jsonObject.getInt("is_run"),
         *     format = jsonObject.getString("format")
         * ) { error ->
         *     logger.warn("Timer creation failed", error)
         * }
         * ```
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
         * Внутренний метод валидации и создания экземпляра.
         *
         * Выполняет строгие проверки параметров и преобразование сырых данных
         * в строго типизированные значения доменной модели. Применяет fail-fast
         * подход с выбросом исключений при некорректных данных для быстрого
         * обнаружения проблем на этапе создания объекта.
         *
         * @throws IllegalArgumentException при null значениях обязательных параметров
         * @throws IllegalStateException при некорректных значениях флагов (отличных от 0 или 1)
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
