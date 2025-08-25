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
        
        assertEquals(true, callbackCalled, "Error callback should be called on validation failure")
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
        assertEquals(120L, result.totalSecondsFromEventStart, "Should return original seconds value")
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
        assertEquals(170L, result.totalSecondsFromEventStart, "Should add delta time: 120 + 50 = 170")
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
        assertEquals(120L, result.totalSecondsFromEventStart, "Should return original value when no time passed")
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
                assertEquals(isRunning == 1, result.isRunning, "isRunning should match parameter")
            }
        }
    }
}