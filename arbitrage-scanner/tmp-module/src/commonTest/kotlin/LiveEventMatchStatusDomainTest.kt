import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class LiveEventMatchStatusDomainTest {

    // ===== FACTORY METHOD TESTS =====

    @Test
    fun `create should create valid instance with all parameters`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "1st Half"
        )

        assertNotNull(statusDomain, "Должен создавать валидный экземпляр")
    }

    @Test
    fun `create should create valid instance with null timer`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = null,
            matchStatusName = "Pre-Match"
        )

        assertNotNull(statusDomain, "Должен создавать валидный экземпляр с null таймером")
    }

    @Test
    fun `create should create valid instance with null match status name`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 60L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = null
        )

        assertNotNull(statusDomain, "Должен создавать валидный экземпляр с null именем статуса")
    }

    @Test
    fun `create should create valid instance with all null optional parameters`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Break,
            liveEventTimerDomain = null,
            matchStatusName = null
        )

        assertNotNull(statusDomain, "Должен создавать валидный экземпляр со всеми null параметрами")
    }

    // ===== APPLY METHOD TESTS - FINISH SCENARIOS =====

    @Test
    fun `apply should return Finish when status is Stopped`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Stopped,
            liveEventTimerDomain = null,
            matchStatusName = "Full Time"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.Finish,
            result,
            "Должен возвращать Finish для статуса Stopped"
        )
    }

    @Test
    fun `apply should return Finish when status is Final`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Final,
            liveEventTimerDomain = null,
            matchStatusName = "Final Result"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.Finish,
            result,
            "Должен возвращать Finish для статуса Final"
        )
    }

    @Test
    fun `apply should return Finish for Final status even with valid timer and match name`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 90L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Final,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "Full Time"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.Finish,
            result,
            "Final статус должен всегда возвращать Finish независимо от других параметров"
        )
    }

    @Test
    fun `apply should return Finish for Stopped status even with valid timer and match name`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 45L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Stopped,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "Stopped"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.Finish,
            result,
            "Stopped статус должен всегда возвращать Finish независимо от других параметров"
        )
    }

    // ===== APPLY METHOD TESTS - NO INFO SCENARIOS =====

    @Test
    fun `apply should return NoInfo when matchStatusName is null`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 30L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = null
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.NoInfo,
            result,
            "Должен возвращать NoInfo когда matchStatusName равен null"
        )
    }

    @Test
    fun `apply should return NoInfo when matchStatusName is empty string`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 30L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = ""
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.NoInfo,
            result,
            "Должен возвращать NoInfo когда matchStatusName пустая строка"
        )
    }

    @Test
    fun `apply should return NoInfo when matchStatusName is blank string`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 30L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "   "
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.NoInfo,
            result,
            "Должен возвращать NoInfo когда matchStatusName содержит только пробелы"
        )
    }

    @Test
    fun `apply should return NoInfo for blank matchStatusName with null timer`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Paused,
            liveEventTimerDomain = null,
            matchStatusName = ""
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.NoInfo,
            result,
            "Должен возвращать NoInfo для пустого имени статуса даже с null таймером"
        )
    }

    // ===== APPLY METHOD TESTS - STATUS VALUE SCENARIOS =====

    @Test
    fun `apply should return StatusValue when timer is null but matchStatusName is valid`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = null,
            matchStatusName = "Pre-Match"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValue(timeName = "Pre-Match"),
            result,
            "Должен возвращать StatusValue с именем статуса когда таймер null"
        )
    }

    @Test
    fun `apply should return StatusValue when timer apply returns null`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 0, // Таймер скрыт, apply вернет null
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "1st Half"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValue(timeName = "1st Half"),
            result,
            "Должен возвращать StatusValue когда timer.apply() возвращает null"
        )
    }

    @Test
    fun `apply should return StatusValue when timer has invalid time calculation`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 3000L, // Время обновления больше чем текущее
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "1st Half"
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L) // Меньше чем timerValueInSecondsMd

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValue(timeName = "1st Half"),
            result,
            "Должен возвращать StatusValue когда таймер не может рассчитать валидное время"
        )
    }

    // ===== APPLY METHOD TESTS - STATUS VALUE WITH TIMER SCENARIOS =====

    @Test
    fun `apply should return StatusValueWithTimer when timer and match status are valid`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "1st Half"
        )

        when (val result = statusDomain.apply(currentSystemTimestamp = 1030L)) {
            is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                assertEquals("1st Half", result.matchStatusName, "Имя статуса должно совпадать")
                assertNotNull(result.liveEventTimerValue, "Значение таймера не должно быть null")
                assertEquals(true, result.liveEventTimerValue.isRunning, "Таймер должен быть активным")
            }

            else -> throw AssertionError("Ожидался StatusValueWithTimer, получен: $result")
        }
    }

    @Test
    fun `apply should return StatusValueWithTimer for static timer`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 90L,
            timerValueInSecondsMd = 1000L,
            isRunning = 0 // Статичный таймер
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Paused,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "2nd Half"
        )

        when (val result = statusDomain.apply(currentSystemTimestamp = 2000L)) {
            is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                assertEquals("2nd Half", result.matchStatusName, "Имя статуса должно совпадать")
                assertNotNull(result.liveEventTimerValue, "Значение таймера не должно быть null")
                assertEquals(false, result.liveEventTimerValue.isRunning, "Таймер должен быть неактивным")
            }

            else -> throw AssertionError("Ожидался StatusValueWithTimer, получен: $result")
        }
    }

    // ===== DIFFERENT LIVE EVENT STATUS TESTS =====

    @Test
    fun `apply should work correctly for all non-final LiveEventStatus values`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 45L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusList = listOf(
            LiveEventStatus.Active,
            LiveEventStatus.Paused,
            LiveEventStatus.Break,
            LiveEventStatus.OnlyExtra,
            LiveEventStatus.Unknown
        )

        statusList.forEach { status ->
            val statusDomain = LiveEventMatchStatusDomain.create(
                liveEventStatus = status,
                liveEventTimerDomain = timerDomain,
                matchStatusName = "Test Status"
            )

            when (val result = statusDomain.apply(currentSystemTimestamp = 1030L)) {
                is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                    assertEquals("Test Status", result.matchStatusName, "Имя статуса для $status")
                    assertNotNull(result.liveEventTimerValue, "Значение таймера для $status")
                }

                else -> throw AssertionError("Ожидался StatusValueWithTimer для $status, получен: $result")
            }
        }
    }

    // ===== EDGE CASES AND INTEGRATION TESTS =====

    @Test
    fun `apply should handle complex scenarios correctly`() {
        // Сценарий: активный статус, активный таймер, валидное имя
        val runningTimer = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 1800L, // 30 минут
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = runningTimer,
            matchStatusName = "1st Half - Extra Time"
        )

        when (val result = statusDomain.apply(currentSystemTimestamp = 1300L)) { // +5 минут
            is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                assertEquals("1st Half - Extra Time", result.matchStatusName)
                assertEquals("35:00", result.liveEventTimerValue.simpleFormat()) // 30:00 + 5:00 = 35:00
                assertEquals(true, result.liveEventTimerValue.isRunning)
            }

            else -> throw AssertionError("Ожидался StatusValueWithTimer с корректными данными")
        }
    }

    @Test
    fun `apply should maintain consistency across multiple calls`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "Kickoff"
        )

        // Несколько вызовов с разным временем
        val timestamps = listOf(1000L, 1060L, 1120L)
        val expectedTimes = listOf("00:00", "01:00", "02:00")

        timestamps.forEachIndexed { index, timestamp ->
            when (val result = statusDomain.apply(currentSystemTimestamp = timestamp)) {
                is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                    assertEquals("Kickoff", result.matchStatusName)
                    assertEquals(expectedTimes[index], result.liveEventTimerValue.simpleFormat())
                    assertEquals(true, result.liveEventTimerValue.isRunning)
                }

                else -> throw AssertionError("Ожидался StatusValueWithTimer для временной метки $timestamp")
            }
        }
    }

    @Test
    fun `apply should work with zero seconds timer value`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 0L,
            timerValueInSecondsMd = 2000L,
            isRunning = 0
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Break,
            liveEventTimerDomain = timerDomain,
            matchStatusName = "Half Time"
        )

        when (val result = statusDomain.apply(currentSystemTimestamp = 3000L)) {
            is LiveEventMatchStatusDomain.LiveEventMatchStatusValue.StatusValueWithTimer -> {
                assertEquals("Half Time", result.matchStatusName)
                assertEquals("00:00", result.liveEventTimerValue.simpleFormat())
                assertEquals(false, result.liveEventTimerValue.isRunning)
            }

            else -> throw AssertionError("Ожидался StatusValueWithTimer с нулевым таймером")
        }
    }

    // ===== DECISION LOGIC PRIORITY TESTS =====

    @Test
    fun `apply should prioritize Finish status over NoInfo matchStatusName`() {
        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Final,
            liveEventTimerDomain = null,
            matchStatusName = null // Это обычно дало бы NoInfo
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.Finish,
            result,
            "Final статус должен иметь приоритет над проверкой matchStatusName"
        )
    }

    @Test
    fun `apply should prioritize NoInfo over timer check when matchStatusName is blank`() {
        val timerDomain = LiveEventTimerDomain.createOrNull(
            isShow = 1,
            timerValueInSeconds = 120L,
            timerValueInSecondsMd = 1000L,
            isRunning = 1
        )

        val statusDomain = LiveEventMatchStatusDomain.create(
            liveEventStatus = LiveEventStatus.Active,
            liveEventTimerDomain = timerDomain, // Валидный таймер
            matchStatusName = "" // Пустое имя
        )

        val result = statusDomain.apply(currentSystemTimestamp = 2000L)

        assertEquals(
            LiveEventMatchStatusDomain.LiveEventMatchStatusValue.NoInfo,
            result,
            "Проверка matchStatusName должна иметь приоритет над проверкой таймера"
        )
    }
}