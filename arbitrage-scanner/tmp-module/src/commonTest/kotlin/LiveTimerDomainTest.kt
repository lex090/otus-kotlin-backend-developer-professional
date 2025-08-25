package bc.core

import LiveTimerDomain
import kotlin.test.Test
import kotlin.test.assertEquals


internal class LiveTimerDomainTest {

    @Test
    fun test1() {
        assertEquals(expected = true, actual = true)
    }

    @Test
    fun test2() {
        var systemTime: Long = 1756126589
        val liveTimerDomain: LiveTimerDomain? = LiveTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120,
            timerValueInSecondsMd = 1756106589,
            isRunning = 1,
            format = "mm:ss",
            onInstanceCreationFailedCallback = {}
        )

        repeat(61) {
            val timerValue = liveTimerDomain?.apply(currentSystemTimestamp = systemTime)
            println("timerValue $timerValue")
            systemTime++
        }
    }
}