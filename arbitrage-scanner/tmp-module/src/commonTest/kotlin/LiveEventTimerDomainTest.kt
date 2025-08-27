package bc.modules.features.api.external.domain.live

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class LiveEventTimerDomainTest {

    // ===== FACTORY METHOD TESTS =====

    @Test
    fun `createOrNull should create valid timer with correct parameters`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNotNull(timer, "Таймер должен создаваться с корректными параметрами")
    }

    @Test
    fun `createOrNull should return null when timerValueInSeconds is null`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = null,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при timerValueInSeconds = null")
    }

    @Test
    fun `createOrNull should return null when timerValueInSecondsMd is null`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = null,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при timerValueInSecondsMd = null")
    }

    /**
     * Пока игнорируем тест на проверку параметра format, по договоренностям с аналитиками.
     * См. Подробный комментарий к классу LiveTimerDomain.
     */
    @Ignore
    @Test
    fun `createOrNull should return null when format is null`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = null
        )

        assertNull(timer, "Таймер должен быть null при format = null")
    }

    /**
     * Пока игнорируем тест на проверку параметра format, по договоренностям с аналитиками.
     * См. Подробный комментарий к классу LiveTimerDomain.
     */
    @Ignore
    @Test
    fun `createOrNull should return null when format is blank`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "   "
        )

        assertNull(timer, "Таймер должен быть null при пустом формате")
    }

    @Test
    fun `createOrNull should return null when timerValueInSeconds is negative`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = -10L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при отрицательном timerValueInSeconds")
    }

    @Test
    fun `createOrNull should return null when timerValueInSecondsMd is zero or negative`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 0L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(
            timer,
            "Таймер должен быть null при timerValueInSecondsMd меньше или равном нулю"
        )
    }

    @Test
    fun `createOrNull should handle invalid isShow values`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 2, // Невалидное значение, только 0 и 1 валидны
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при некорректном значении isShow")
    }

    @Test
    fun `createOrNull should handle invalid isRunning values`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 3, // Невалидное значение, только 0 и 1 валидны
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при некорректном значении isRunning")
    }

    @Test
    fun `createOrNull should return null when isShow is null`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = null,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при isShow = null")
    }

    @Test
    fun `createOrNull should return null when isRunning is null`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = null,
            format = "mm:ss"
        )

        assertNull(timer, "Таймер должен быть null при isRunning = null")
    }

    /**
     * Пока игнорируем тест на проверку параметра format, по договоренностям с аналитиками.
     * См. Подробный комментарий к классу LiveTimerDomain.
     */
    @Ignore
    @Test
    fun `createOrNull should return null when format is empty string`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = ""
        )

        assertNull(timer, "Таймер должен быть null при пустой строке формата")
    }

    @Test
    fun `createOrNull should call error callback when creation fails`() {
        var callbackCalled = false

        LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = -10L, // Это должно вызвать ошибку валидации
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = { _ ->
                callbackCalled = true
            }
        )

        assertEquals(
            true,
            callbackCalled,
            "Callback ошибки должен вызываться при неудачной валидации"
        )
    }

    @Test
    fun `createOrNull should capture specific error messages in callback`() {
        val capturedMessages = mutableListOf<String>()

        // Тестирование сообщения об ошибке для отрицательного timerValueInSeconds
        LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = -5L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = { error ->
                capturedMessages.add(error.message ?: "")
            }
        )

        assertEquals(1, capturedMessages.size, "Должна быть зафиксирована одна ошибка")
    }

    // ===== MAIN LOGIC TESTS =====

    @Test
    fun `apply should return null when timer is not shown`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 0, // Таймер не должен показываться
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNull(result, "apply() должен возвращать null когда таймер не показан")
    }

    @Test
    fun `apply should return static timer value when not running`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Таймер не активен
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "apply() должен возвращать результат для статичного таймера")
        assertEquals(false, result.isRunning, "Таймер не должен быть активным")
        assertEquals(
            "02:00",
            result.simpleFormat(),
            "Должен возвращать отформатированное исходное значение (120s = 02:00)"
        )
    }

    @Test
    fun `apply should calculate running timer correctly`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1, // Таймер активен
            format = "mm:ss"
        )

        val currentTime = 1050L // 50 секунд после последнего обновления
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result, "apply() должен возвращать результат для активного таймера")
        assertEquals(true, result.isRunning, "Таймер должен быть активным")
        assertEquals(
            "02:50",
            result.simpleFormat(),
            "Должен добавлять дельту времени и корректно форматировать: 170s = 02:50"
        )
    }

    @Test
    fun `apply should return null when system time is less than update time`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 2000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = 1500L // Меньше чем время обновления (2000L)
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNull(result, "apply() должен возвращать null когда системное время сдвинулось назад")
    }

    @Test
    fun `apply should handle zero delta time correctly`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = 1000L // То же, что и время обновления
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result, "apply() должен обрабатывать нулевую дельту времени")
        assertEquals(
            "02:00",
            result.simpleFormat(),
            "Должен возвращать исходное отформатированное значение когда время не прошло (120s = 02:00)"
        )
    }

    // ===== EDGE CASES =====

    @Test
    fun `apply should work with large time values`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = Long.MAX_VALUE / 2,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Use static timer to avoid overflow
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "apply() должен обрабатывать большие значения")
        assertEquals(
            true,
            result.simpleFormat().isNotBlank(),
            "Должен возвращать отформатированное значение для больших чисел"
        )
    }

    @Test
    fun `timer should work with different boolean parameter combinations`() {
        // Test all combinations of isShow and isRunning
        val testCases = listOf(
            Pair(0, 0), // Hidden, not running
            Pair(0, 1), // Hidden, running
            Pair(1, 0), // Shown, not running
            Pair(1, 1)  // Shown, running
        )

        testCases.forEach { (isShow, isRunning) ->
            val timer = LiveEventTimerDomain.Companion.createOrNull(
                isShow = isShow,
                timerValueInSeconds = 60L,
                timerValueInSecondsMd = 1000L,
                isRunning = isRunning,
                format = "mm:ss"
            )

            assertNotNull(
                timer,
                "Таймер должен создаваться для isShow=$isShow, isRunning=$isRunning"
            )

            val result = timer.apply(currentSystemTimestamp = 1030L)

            if (isShow == 0) {
                assertNull(result, "Результат должен быть null при isShow=0")
            } else {
                assertNotNull(result, "Результат не должен быть null при isShow=1")
                assertEquals(
                    isRunning == 1,
                    result.isRunning,
                    "isRunning должен соответствовать параметру"
                )
            }
        }
    }

    // ===== OVERFLOW AND EDGE CASE TESTS =====

    @Test
    fun `apply should handle potential overflow in running timer calculation`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = Long.MAX_VALUE - 100L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Try to cause overflow by adding large delta
        val currentTime = 1200L // deltaTime = 200, would cause overflow
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        // With overflow protection, should return null to prevent showing incorrect time
        assertNull(
            result,
            "Должен возвращать null при переполнении для защиты UI от некорректных данных"
        )
    }

    @Test
    fun `apply should work with secondsFromEventStart exactly zero`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L, // Exactly zero
            timerValueInSecondsMd = 1000L,
            isRunning = 0,
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "Таймер должен работать с нулевыми секундами")
        assertEquals(
            "00:00",
            result.simpleFormat(),
            "Должен возвращать ноль отформатированный как 00:00"
        )
        assertEquals(false, result.isRunning, "Не должен быть активным")
    }

    @Test
    fun `apply should work with zero deltaTime for running timer`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 300L,
            timerValueInSecondsMd = 5000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Exact same timestamp as update time
        val result = timer?.apply(currentSystemTimestamp = 5000L)

        assertNotNull(result, "Таймер должен работать с нулевой дельтой")
        assertEquals(
            "05:00",
            result.simpleFormat(),
            "Должен возвращать исходное отформатированное значение (300s = 05:00)"
        )
        assertEquals(true, result.isRunning, "Должен быть активным")
    }

    @Test
    fun `apply should handle very large deltaTime values`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 100L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = Long.MAX_VALUE / 2 // Very large timestamp
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result)
        assertEquals(
            true, result.simpleFormat().isNotBlank(),
            "Если результат существует, должно быть отформатированное значение"
        )
    }

    @Test
    fun `apply should handle edge case with maximum valid secondsFromEventStartMd`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 60L,
            timerValueInSecondsMd = Long.MAX_VALUE / 2,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = Long.MAX_VALUE / 2 + 100L
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result)
        assertEquals(
            true, result.simpleFormat().isNotBlank(),
            "Должен обрабатывать большие значения временных меток и возвращать отформатированный вывод"
        )
    }

    // ===== DATA CLASS LiveTimerValue TESTS =====

    @Test
    fun `LiveTimerValue should work with different field combinations`() {
        val testCases = listOf(
            Pair(true, 0L),
            Pair(false, Long.MAX_VALUE),
            Pair(true, 3600L),
            Pair(false, 1L)
        )

        testCases.forEach { (isRunning, seconds) ->
            val value = LiveEventTimerDomain.LiveEventTimerValue(
                isRunning = isRunning,
                totalSecondsFromEventStart = seconds
            )

            assertEquals(isRunning, value.isRunning, "isRunning должен соответствовать")
            assertEquals(
                true,
                value.simpleFormat().isNotBlank(),
                "Должен суметь отформатировать любое валидное значение секунд"
            )
        }
    }

    @Test
    fun `LiveTimerValue simpleFormat should format time correctly`() {
        val testCases = listOf(
            Pair(0L, "00:00"),
            Pair(59L, "00:59"),
            Pair(60L, "01:00"),
            Pair(120L, "02:00"),
            Pair(3661L, "61:01"), // 1 час 1 минута 1 секунда = 61:01
            Pair(7200L, "120:00") // 2 часа = 120:00
        )

        testCases.forEach { (seconds, expectedFormat) ->
            val value = LiveEventTimerDomain.LiveEventTimerValue(
                isRunning = true,
                totalSecondsFromEventStart = seconds
            )

            assertEquals(
                expectedFormat,
                value.simpleFormat(),
                "Формат для $seconds секунд должен быть $expectedFormat"
            )
        }
    }

    // ===== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ =====

    @Test
    fun `simpleFormat should handle edge cases for time formatting`() {
        val testCases = listOf(
            Pair(1L, "00:01"), // 1 секунда
            Pair(59L, "00:59"), // 59 секунд
            Pair(61L, "01:01"), // 1 минута 1 секунда
            Pair(599L, "09:59"), // 9 минут 59 секунд
            Pair(600L, "10:00"), // 10 минут
            Pair(3599L, "59:59"), // 59 минут 59 секунд
            Pair(3600L, "60:00"), // 1 час = 60:00 в формате mm:ss
            Pair(36000L, "600:00"), // 10 часов = 600:00 в формате mm:ss
        )

        testCases.forEach { (seconds, expected) ->
            val value = LiveEventTimerDomain.LiveEventTimerValue(
                isRunning = true,
                totalSecondsFromEventStart = seconds
            )
            assertEquals(
                expected,
                value.simpleFormat(),
                "Секунды $seconds должны форматироваться как $expected"
            )
        }
    }

    @Test
    fun `createOrNull should work with boundary values`() {
        // Граничные значения для timerValueInSeconds
        val validTimer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L, // Минимальное валидное значение
            timerValueInSecondsMd = 1L, // Минимальное валидное значение
            isRunning = 0,
            format = "mm:ss"
        )
        assertNotNull(validTimer, "Должен создаваться таймер с граничными валидными значениями")

        val maxTimer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = Long.MAX_VALUE,
            timerValueInSecondsMd = Long.MAX_VALUE,
            isRunning = 0,
            format = "mm:ss"
        )
        assertNotNull(maxTimer, "Должен создаваться таймер с максимальными значениями")
    }

    @Test
    fun `apply should handle concurrent timer updates correctly`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 100L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Симуляция множественных вызовов apply с разными временными метками
        val results = listOf(1010L, 1020L, 1030L).map { timestamp ->
            timer?.apply(currentSystemTimestamp = timestamp)
        }

        results.forEach { result ->
            assertNotNull(result, "Все результаты должны быть не null")
            assertEquals(true, result.isRunning, "Таймер должен оставаться активным")
        }

        // Проверяем, что время увеличивается правильно
        assertEquals("01:50", results[0]?.simpleFormat()) // 100 + 10 = 110s = 01:50
        assertEquals("02:00", results[1]?.simpleFormat()) // 100 + 20 = 120s = 02:00
        assertEquals("02:10", results[2]?.simpleFormat()) // 100 + 30 = 130s = 02:10
    }

    @Test
    fun `apply should return different objects for different timestamps`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 50L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val result1 = timer?.apply(currentSystemTimestamp = 1010L)
        val result2 = timer?.apply(currentSystemTimestamp = 1020L)

        assertNotNull(result1)
        assertNotNull(result2)

        // Объекты должны быть разными и иметь разное время
        assertEquals(false, result1 === result2, "Должны быть разные объекты")
        assertEquals("01:00", result1.simpleFormat()) // 50 + 10 = 60s
        assertEquals("01:10", result2.simpleFormat()) // 50 + 20 = 70s
    }

    @Test
    fun `callback should be called for various validation failures`() {
        val errorMessages = mutableListOf<String>()
        val callback: (Throwable) -> Unit = { error ->
            errorMessages.add(error.message ?: "")
        }

        // Тестируем различные виды ошибок валидации
        LiveEventTimerDomain.Companion.createOrNull(
            isShow = null,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = callback
        )

        LiveEventTimerDomain.Companion.createOrNull(
            isShow = 2, // Невалидное значение
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = callback
        )

        LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = -1L, // Невалидное значение
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = callback
        )

        assertEquals(
            true,
            errorMessages.size >= 3,
            "Должно быть зафиксировано минимум 3 ошибки валидации"
        )
    }

    @Test
    fun `simpleFormat should be consistent across different LiveTimerValue instances`() {
        val timer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 125L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val result1 = timer?.apply(currentSystemTimestamp = 1005L) // 125 + 5 = 130s
        val result2 = timer?.apply(currentSystemTimestamp = 1005L) // То же время

        assertNotNull(result1)
        assertNotNull(result2)

        assertEquals(
            result1.simpleFormat(),
            result2.simpleFormat(),
            "Одинаковые временные метки должны давать одинаковое форматирование"
        )

        assertEquals("02:10", result1.simpleFormat()) // 130s = 2:10
    }

    // ===== ТЕСТЫ ДЛЯ EQUALS() И HASHCODE() =====

    @Test
    fun `LiveTimerDomain equals should work correctly`() {
        val timer1 = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer2 = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer3 = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 0, // Другое значение
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNotNull(timer1)
        assertNotNull(timer2)
        assertNotNull(timer3)

        // Проверка равенства
        assertEquals(timer1, timer2, "Таймеры с одинаковыми параметрами должны быть равны")
        assertEquals(false, timer1 == timer3, "Таймеры с разными параметрами должны быть не равны")

        // Проверка рефлексивности
        assertEquals(timer1, timer1, "Объект должен быть равен самому себе")

        // Проверка с null
        assertEquals(false, timer1.equals(null), "Объект не должен быть равен null")

        // Проверка с объектом другого типа
        assertEquals(
            false,
            timer1.equals("строка"),
            "Объект не должен быть равен объекту другого типа"
        )
    }

    @Test
    fun `LiveTimerDomain hashCode should work correctly`() {
        val timer1 = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer2 = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNotNull(timer1)
        assertNotNull(timer2)

        // Равные объекты должны иметь одинаковый hashCode
        assertEquals(
            timer1.hashCode(),
            timer2.hashCode(),
            "Равные объекты должны иметь одинаковый hashCode"
        )
    }

    @Test
    fun `LiveTimerValue equals should work correctly`() {
        val value1 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value3 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = false,
            totalSecondsFromEventStart = 120L
        )

        // Проверка равенства
        assertEquals(value1, value2, "Значения с одинаковыми параметрами должны быть равны")
        assertEquals(false, value1 == value3, "Значения с разным isRunning должны быть не равны")

        // Проверка рефлексивности
        assertEquals(value1, value1, "Объект должен быть равен самому себе")

        // Проверка с null
        assertEquals(false, value1.equals(null), "Объект не должен быть равен null")

        // Проверка с объектом другого типа
        assertEquals(
            false,
            value1.equals("строка"),
            "Объект не должен быть равен объекту другого типа"
        )
    }

    @Test
    fun `LiveTimerValue hashCode should work correctly`() {
        val value1 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        // Равные объекты должны иметь одинаковый hashCode
        assertEquals(
            value1.hashCode(),
            value2.hashCode(),
            "Равные объекты должны иметь одинаковый hashCode"
        )
    }

    @Test
    fun `LiveTimerDomain equals should test all field differences`() {
        val baseTimer = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в secondsFromEventStart
        val differentSeconds = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 130L, // Разное значение
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в secondsFromEventStartMd
        val differentMd = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1100L, // Разное значение
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в isRunning
        val differentRunning = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Разное значение
            format = "mm:ss"
        )

        // Тест различия в format
        val differentFormat = LiveEventTimerDomain.Companion.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "hh:mm:ss" // Разное значение
        )

        assertNotNull(baseTimer)
        assertNotNull(differentSeconds)
        assertNotNull(differentMd)
        assertNotNull(differentRunning)
        assertNotNull(differentFormat)

        // Проверяем, что все различия обнаруживаются
        assertEquals(false, baseTimer == differentSeconds, "Различие в secondsFromEventStart")
        assertEquals(false, baseTimer == differentMd, "Различие в secondsFromEventStartMd")
        assertEquals(false, baseTimer == differentRunning, "Различие в isRunning")
        /**
         * Пока игнорируем тест параметра format, по договоренностям с аналитиками.
         * См. Подробный комментарий к классу LiveTimerDomain.
         * @Ignore
         */
//        assertEquals(false, baseTimer == differentFormat, "Различие в format")
    }

    @Test
    fun `LiveTimerValue equals should test totalSecondsFromEventStart difference`() {
        val value1 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveEventTimerDomain.LiveEventTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 130L // Разное значение
        )

        assertEquals(
            false,
            value1 == value2,
            "Значения с разным totalSecondsFromEventStart должны быть не равны"
        )
    }
}