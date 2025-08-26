package bc.core

import LiveTimerDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


internal class LiveTimerDomainTest {

    // ===== FACTORY METHOD TESTS =====

    @Test
    fun `createOrNull should create valid timer with correct parameters`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNotNull(timer, "Timer should be created with valid parameters")
    }

    @Test
    fun `createOrNull should return null when timerValueInSeconds is null`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = null,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when timerValueInSeconds is null")
    }

    @Test
    fun `createOrNull should return null when timerValueInSecondsMd is null`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = null,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when timerValueInSecondsMd is null")
    }

    @Test
    fun `createOrNull should return null when format is null`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = null
        )

        assertNull(timer, "Timer should be null when format is null")
    }

    @Test
    fun `createOrNull should return null when format is blank`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "   "
        )

        assertNull(timer, "Timer should be null when format is blank")
    }

    @Test
    fun `createOrNull should return null when timerValueInSeconds is negative`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = -10L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when timerValueInSeconds is negative")
    }

    @Test
    fun `createOrNull should return null when timerValueInSecondsMd is zero or negative`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 0L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when timerValueInSecondsMd is zero or negative")
    }

    @Test
    fun `createOrNull should handle invalid isShow values`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 2, // Invalid value, only 0 and 1 are valid
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when isShow has invalid value")
    }

    @Test
    fun `createOrNull should handle invalid isRunning values`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 3, // Invalid value, only 0 and 1 are valid
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when isRunning has invalid value")
    }

    @Test
    fun `createOrNull should return null when isShow is null`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = null,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when isShow is null")
    }

    @Test
    fun `createOrNull should return null when isRunning is null`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = null,
            format = "mm:ss"
        )

        assertNull(timer, "Timer should be null when isRunning is null")
    }

    @Test
    fun `createOrNull should return null when format is empty string`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = ""
        )

        assertNull(timer, "Timer should be null when format is empty string")
    }

    @Test
    fun `createOrNull should call error callback when creation fails`() {
        var callbackCalled = false

        LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = -10L, // This should cause validation error
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
            "Error callback should be called on validation failure"
        )
    }

    @Test
    fun `createOrNull should capture specific error messages in callback`() {
        val capturedMessages = mutableListOf<String>()

        // Test negative timerValueInSeconds error message
        LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = -5L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = { error ->
                capturedMessages.add(error.message ?: "")
            }
        )

        assertEquals(1, capturedMessages.size, "Should capture one error")
    }

    // ===== MAIN LOGIC TESTS =====

    @Test
    fun `apply should return null when timer is not shown`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 0, // Timer should not be shown
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNull(result, "apply() should return null when timer is not shown")
    }

    @Test
    fun `apply should return static timer value when not running`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Timer is not running
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "apply() should return result for static timer")
        assertEquals(false, result.isRunning, "Timer should not be running")
        assertEquals(
            "02:00",
            result.simpleFormat(),
            "Should return formatted original value (120s = 02:00)"
        )
    }

    @Test
    fun `apply should calculate running timer correctly`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1, // Timer is running
            format = "mm:ss"
        )

        val currentTime = 1050L // 50 seconds after last update
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result, "apply() should return result for running timer")
        assertEquals(true, result.isRunning, "Timer should be running")
        assertEquals(
            "02:50",
            result.simpleFormat(),
            "Should add delta time and format correctly: 170s = 02:50"
        )
    }

    @Test
    fun `apply should return null when system time is less than update time`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 2000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = 1500L // Less than update time (2000L)
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNull(result, "apply() should return null when system time went backwards")
    }

    @Test
    fun `apply should handle zero delta time correctly`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = 1000L // Same as update time
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        assertNotNull(result, "apply() should handle zero delta time")
        assertEquals(
            "02:00",
            result.simpleFormat(),
            "Should return original formatted value when no time passed (120s = 02:00)"
        )
    }

    // ===== EDGE CASES =====

    @Test
    fun `apply should work with large time values`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = Long.MAX_VALUE / 2,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Use static timer to avoid overflow
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "apply() should handle large values")
        assertEquals(true, result.simpleFormat().isNotBlank(), "Should return formatted value for large numbers")
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
            val timer = LiveTimerDomain.createOrNull(
                isShow = isShow,
                timerValueInSeconds = 60L,
                timerValueInSecondsMd = 1000L,
                isRunning = isRunning,
                format = "mm:ss"
            )

            assertNotNull(timer, "Timer should be created for isShow=$isShow, isRunning=$isRunning")

            val result = timer.apply(currentSystemTimestamp = 1030L)

            if (isShow == 0) {
                assertNull(result, "Result should be null when isShow=0")
            } else {
                assertNotNull(result, "Result should not be null when isShow=1")
                assertEquals(
                    isRunning == 1,
                    result.isRunning,
                    "isRunning should match parameter"
                )
            }
        }
    }

    // ===== OVERFLOW AND EDGE CASE TESTS =====

    @Test
    fun `apply should handle potential overflow in running timer calculation`() {
        val timer = LiveTimerDomain.createOrNull(
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
            "Should return null when overflow occurs to protect UI from incorrect data"
        )
    }

    @Test
    fun `apply should work with secondsFromEventStart exactly zero`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L, // Exactly zero
            timerValueInSecondsMd = 1000L,
            isRunning = 0,
            format = "mm:ss"
        )

        val result = timer?.apply(currentSystemTimestamp = 2000L)

        assertNotNull(result, "Timer should work with zero seconds")
        assertEquals("00:00", result.simpleFormat(), "Should return zero formatted as 00:00")
        assertEquals(false, result.isRunning, "Should not be running")
    }

    @Test
    fun `apply should work with zero deltaTime for running timer`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 300L,
            timerValueInSecondsMd = 5000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Exact same timestamp as update time
        val result = timer?.apply(currentSystemTimestamp = 5000L)

        assertNotNull(result, "Timer should work with zero delta")
        assertEquals(
            "05:00",
            result.simpleFormat(),
            "Should return original formatted value (300s = 05:00)"
        )
        assertEquals(true, result.isRunning, "Should be running")
    }

    @Test
    fun `apply should handle very large deltaTime values`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 100L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = Long.MAX_VALUE / 2 // Very large timestamp
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        // Should either handle gracefully or return null
        if (result != null) {
            assertEquals(
                true, result.simpleFormat().isNotBlank(),
                "If result exists, should have formatted value"
            )
        }
    }

    @Test
    fun `apply should handle edge case with maximum valid secondsFromEventStartMd`() {
        val timer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 60L,
            timerValueInSecondsMd = Long.MAX_VALUE / 2,
            isRunning = 1,
            format = "mm:ss"
        )

        val currentTime = Long.MAX_VALUE / 2 + 100L
        val result = timer?.apply(currentSystemTimestamp = currentTime)

        if (result != null) {
            assertEquals(
                true, result.simpleFormat().isNotBlank(),
                "Should handle large timestamp values and return formatted output"
            )
        }
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
            val value = LiveTimerDomain.LiveTimerValue(
                isRunning = isRunning,
                totalSecondsFromEventStart = seconds
            )

            assertEquals(isRunning, value.isRunning, "isRunning should match")
            assertEquals(true, value.simpleFormat().isNotBlank(), "Should be able to format any valid seconds value")
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
            val value = LiveTimerDomain.LiveTimerValue(
                isRunning = true,
                totalSecondsFromEventStart = seconds
            )

            assertEquals(
                expectedFormat,
                value.simpleFormat(),
                "Format for $seconds seconds should be $expectedFormat"
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
            val value = LiveTimerDomain.LiveTimerValue(
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
        val validTimer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L, // Минимальное валидное значение
            timerValueInSecondsMd = 1L, // Минимальное валидное значение
            isRunning = 0,
            format = "mm:ss"
        )
        assertNotNull(validTimer, "Должен создаваться таймер с граничными валидными значениями")

        val maxTimer = LiveTimerDomain.createOrNull(
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
        val timer = LiveTimerDomain.createOrNull(
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
        val timer = LiveTimerDomain.createOrNull(
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
    fun `timer creation with different format strings`() {
        val formats = listOf("mm:ss", "hh:mm:ss", "ss", "custom", "h:m:s", "00:00:00")

        formats.forEach { format ->
            val timer = LiveTimerDomain.createOrNull(
                isShow = 1,
                timerValueInSeconds = 120L,
                timerValueInSecondsMd = 1000L,
                isRunning = 0,
                format = format
            )
            assertNotNull(timer, "Таймер должен создаваться с форматом $format")
        }
    }

    @Test
    fun `callback should be called for various validation failures`() {
        val errorMessages = mutableListOf<String>()
        val callback: (Throwable) -> Unit = { error ->
            errorMessages.add(error.message ?: "")
        }

        // Тестируем различные виды ошибок валидации
        LiveTimerDomain.createOrNull(
            isShow = null,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = callback
        )

        LiveTimerDomain.createOrNull(
            isShow = 2, // Невалидное значение
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = callback
        )

        LiveTimerDomain.createOrNull(
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
        val timer = LiveTimerDomain.createOrNull(
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
        val timer1 = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer2 = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer3 = LiveTimerDomain.createOrNull(
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
        assertEquals(false, timer1.equals("строка"), "Объект не должен быть равен объекту другого типа")
    }

    @Test
    fun `LiveTimerDomain hashCode should work correctly`() {
        val timer1 = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        val timer2 = LiveTimerDomain.createOrNull(
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
        val value1 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value3 = LiveTimerDomain.LiveTimerValue(
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
        assertEquals(false, value1.equals("строка"), "Объект не должен быть равен объекту другого типа")
    }

    @Test
    fun `LiveTimerValue hashCode should work correctly`() {
        val value1 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveTimerDomain.LiveTimerValue(
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
        val baseTimer = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в secondsFromEventStart
        val differentSeconds = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 130L, // Разное значение
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в secondsFromEventStartMd
        val differentMd = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1100L, // Разное значение
            isRunning = 1,
            format = "mm:ss"
        )

        // Тест различия в isRunning
        val differentRunning = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0, // Разное значение
            format = "mm:ss"
        )

        // Тест различия в format
        val differentFormat = LiveTimerDomain.createOrNull(
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
        assertEquals(false, baseTimer == differentFormat, "Различие в format")
    }

    @Test
    fun `LiveTimerValue equals should test totalSecondsFromEventStart difference`() {
        val value1 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L
        )

        val value2 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 130L // Разное значение
        )

        assertEquals(false, value1 == value2, "Значения с разным totalSecondsFromEventStart должны быть не равны")
    }
}