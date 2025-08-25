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
    fun `createOrNull should call error callback when creation fails`() {
        var callbackCalled = false
        var capturedError: Throwable? = null

        LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = -10L, // This should cause validation error
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = { error ->
                callbackCalled = true
                capturedError = error
            }
        )

        assertEquals(
            true,
            callbackCalled,
            "Error callback should be called on validation failure"
        )
        assertNotNull(capturedError, "Error should be captured in callback")
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
            120L,
            result.totalSecondsFromEventStart,
            "Should return original seconds value"
        )
        assertEquals("mm:ss", result.format, "Should preserve format")
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
            170L,
            result.totalSecondsFromEventStart,
            "Should add delta time: 120 + 50 = 170"
        )
        assertEquals("mm:ss", result.format, "Should preserve format")
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
            120L,
            result.totalSecondsFromEventStart,
            "Should return original value when no time passed"
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
        assertEquals(Long.MAX_VALUE / 2, result.totalSecondsFromEventStart)
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

    // ===== ADDITIONAL FACTORY METHOD TESTS FOR 100% COVERAGE =====

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
        assertEquals(
            true, capturedMessages[0].contains("должен быть больше или равен 0"),
            "Should contain correct error message for negative timerValueInSeconds"
        )
    }

    @Test
    fun `createOrNull should capture error message for invalid secondsFromEventStartMd`() {
        var capturedMessage: String? = null

        LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = -1L,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = { error ->
                capturedMessage = error.message
            }
        )

        assertNotNull(capturedMessage, "Should capture error message")
        assertEquals(
            true, capturedMessage!!.contains("должен быть больше 0"),
            "Should contain correct error message for invalid secondsFromEventStartMd"
        )
    }

    @Test
    fun `createOrNull should capture error message for blank format`() {
        var capturedMessage: String? = null

        LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1,
            format = "   ",
            onInstanceCreationFailedCallback = { error ->
                capturedMessage = error.message
            }
        )

        assertNotNull(capturedMessage, "Should capture error message")
        assertEquals(
            true, capturedMessage!!.contains("не должен быть пустым"),
            "Should contain correct error message for blank format"
        )
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
        assertEquals(0L, result.totalSecondsFromEventStart, "Should return zero seconds")
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
            300L,
            result.totalSecondsFromEventStart,
            "Should return original value"
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
                true, result.totalSecondsFromEventStart >= 100L,
                "If result exists, should be at least original value"
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
                true, result.totalSecondsFromEventStart >= 60L,
                "Should handle large timestamp values"
            )
        }
    }

    // ===== DATA CLASS LiveTimerValue TESTS =====

    @Test
    fun `LiveTimerValue should have correct equals implementation`() {
        val value1 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        val value2 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        val value3 = LiveTimerDomain.LiveTimerValue(
            isRunning = false,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        assertEquals(value1, value2, "Same values should be equal")
        assertEquals(false, value1 == value3, "Different values should not be equal")
    }

    @Test
    fun `LiveTimerValue should have correct hashCode implementation`() {
        val value1 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        val value2 = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        assertEquals(
            value1.hashCode(), value2.hashCode(),
            "Equal objects should have equal hash codes"
        )
    }

    @Test
    fun `LiveTimerValue should have correct toString implementation`() {
        val value = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        val toString = value.toString()

        assertEquals(
            true,
            toString.contains("LiveTimerValue"),
            "Should contain class name"
        )
        assertEquals(
            true,
            toString.contains("isRunning=true"),
            "Should contain isRunning field"
        )
        assertEquals(
            true,
            toString.contains("totalSecondsFromEventStart=120"),
            "Should contain totalSecondsFromEventStart field"
        )
        assertEquals(
            true,
            toString.contains("format=mm:ss"),
            "Should contain format field"
        )
    }

    @Test
    fun `LiveTimerValue should support copy functionality`() {
        val original = LiveTimerDomain.LiveTimerValue(
            isRunning = true,
            totalSecondsFromEventStart = 120L,
            format = "mm:ss"
        )

        val copied = original.copy(isRunning = false)

        assertEquals(false, copied.isRunning, "Copied value should have modified field")
        assertEquals(
            120L,
            copied.totalSecondsFromEventStart,
            "Copied value should preserve other fields"
        )
        assertEquals("mm:ss", copied.format, "Copied value should preserve other fields")
        assertEquals(true, original.isRunning, "Original should remain unchanged")
    }

    @Test
    fun `LiveTimerValue should work with different field combinations`() {
        val testCases = listOf(
            Triple(true, 0L, "hh:mm:ss"),
            Triple(false, Long.MAX_VALUE, "mm:ss"),
            Triple(true, 3600L, "ss"),
            Triple(false, 1L, "custom format")
        )

        testCases.forEach { (isRunning, seconds, format) ->
            val value = LiveTimerDomain.LiveTimerValue(
                isRunning = isRunning,
                totalSecondsFromEventStart = seconds,
                format = format
            )

            assertEquals(isRunning, value.isRunning, "isRunning should match")
            assertEquals(seconds, value.totalSecondsFromEventStart, "seconds should match")
            assertEquals(format, value.format, "format should match")
        }
    }
}