import kotlin.test.Test
import kotlin.test.assertEquals

internal class LiveEventStatusTest {

    // ===== FACTORY METHOD TESTS =====

    @Test
    fun `create should return Active for status 0`() {
        val status = LiveEventStatus.create(eventStatus = 0)
        assertEquals(LiveEventStatus.Active, status, "Статус 0 должен возвращать Active")
    }

    @Test
    fun `create should return Paused for status 1`() {
        val status = LiveEventStatus.create(eventStatus = 1)
        assertEquals(LiveEventStatus.Paused, status, "Статус 1 должен возвращать Paused")
    }

    @Test
    fun `create should return Break for status 2`() {
        val status = LiveEventStatus.create(eventStatus = 2)
        assertEquals(LiveEventStatus.Break, status, "Статус 2 должен возвращать Break")
    }

    @Test
    fun `create should return Stopped for status 3`() {
        val status = LiveEventStatus.create(eventStatus = 3)
        assertEquals(LiveEventStatus.Stopped, status, "Статус 3 должен возвращать Stopped")
    }

    @Test
    fun `create should return Final for status 4`() {
        val status = LiveEventStatus.create(eventStatus = 4)
        assertEquals(LiveEventStatus.Final, status, "Статус 4 должен возвращать Final")
    }

    @Test
    fun `create should return OnlyExtra for status 5`() {
        val status = LiveEventStatus.create(eventStatus = 5)
        assertEquals(LiveEventStatus.OnlyExtra, status, "Статус 5 должен возвращать OnlyExtra")
    }

    // ===== UNKNOWN STATUS TESTS =====

    @Test
    fun `create should return Unknown for null status`() {
        val status = LiveEventStatus.create(eventStatus = null)
        assertEquals(LiveEventStatus.Unknown, status, "null статус должен возвращать Unknown")
    }

    @Test
    fun `create should return Unknown for negative status`() {
        val status = LiveEventStatus.create(eventStatus = -1)
        assertEquals(LiveEventStatus.Unknown, status, "Отрицательный статус должен возвращать Unknown")
    }

    @Test
    fun `create should return Unknown for status greater than 5`() {
        val status = LiveEventStatus.create(eventStatus = 6)
        assertEquals(LiveEventStatus.Unknown, status, "Статус больше 5 должен возвращать Unknown")
    }

    @Test
    fun `create should return Unknown for large positive status`() {
        val status = LiveEventStatus.create(eventStatus = 999)
        assertEquals(LiveEventStatus.Unknown, status, "Большой положительный статус должен возвращать Unknown")
    }

    @Test
    fun `create should return Unknown for large negative status`() {
        val status = LiveEventStatus.create(eventStatus = -999)
        assertEquals(LiveEventStatus.Unknown, status, "Большой отрицательный статус должен возвращать Unknown")
    }

    // ===== EDGE CASES =====

    @Test
    fun `create should return Unknown for Integer MAX_VALUE`() {
        val status = LiveEventStatus.create(eventStatus = Int.MAX_VALUE)
        assertEquals(LiveEventStatus.Unknown, status, "Int.MAX_VALUE должен возвращать Unknown")
    }

    @Test
    fun `create should return Unknown for Integer MIN_VALUE`() {
        val status = LiveEventStatus.create(eventStatus = Int.MIN_VALUE)
        assertEquals(LiveEventStatus.Unknown, status, "Int.MIN_VALUE должен возвращать Unknown")
    }

    // ===== COMPREHENSIVE MAPPING TEST =====

    @Test
    fun `create should map all valid statuses correctly`() {
        val expectedMappings = mapOf(
            0 to LiveEventStatus.Active,
            1 to LiveEventStatus.Paused,
            2 to LiveEventStatus.Break,
            3 to LiveEventStatus.Stopped,
            4 to LiveEventStatus.Final,
            5 to LiveEventStatus.OnlyExtra
        )

        expectedMappings.forEach { (input, expected) ->
            val actual = LiveEventStatus.create(eventStatus = input)
            assertEquals(expected, actual, "Статус $input должен соответствовать $expected")
        }
    }

    @Test
    fun `create should return Unknown for all invalid statuses`() {
        val invalidStatuses = listOf(
            null, -1, -2, -10, -100, -999,
            6, 7, 10, 100, 999,
            Int.MAX_VALUE, Int.MIN_VALUE
        )

        invalidStatuses.forEach { invalidStatus ->
            val actual = LiveEventStatus.create(eventStatus = invalidStatus)
            assertEquals(
                LiveEventStatus.Unknown,
                actual,
                "Невалидный статус $invalidStatus должен возвращать Unknown"
            )
        }
    }

    // ===== STATUS TYPES VERIFICATION =====

    @Test
    fun `status objects should have correct toString representation`() {
        // Проверяем, что toString работает корректно для sealed interface
        val statuses = mapOf(
            LiveEventStatus.Active to "Active",
            LiveEventStatus.Paused to "Paused",
            LiveEventStatus.Break to "Break",
            LiveEventStatus.Stopped to "Stopped",
            LiveEventStatus.Final to "Final",
            LiveEventStatus.OnlyExtra to "OnlyExtra",
            LiveEventStatus.Unknown to "Unknown"
        )

        statuses.forEach { (status, expectedName) ->
            val actualName = status.toString()
            assertEquals(
                true,
                actualName.contains(expectedName),
                "toString для $expectedName должен содержать имя класса"
            )
        }
    }

    // ===== MULTIPLE CALLS CONSISTENCY =====

    @Test
    fun `create should return same instance for same input across multiple calls`() {
        val input = 0
        val status1 = LiveEventStatus.create(eventStatus = input)
        val status2 = LiveEventStatus.create(eventStatus = input)

        assertEquals(status1, status2, "Одинаковый input должен возвращать равные объекты")
        assertEquals(true, status1 === status2, "Должны быть одним и тем же объектом (object)")
    }

    @Test
    fun `create should be consistent across all valid mappings on multiple calls`() {
        val validInputs = listOf(0, 1, 2, 3, 4, 5)

        validInputs.forEach { input ->
            val status1 = LiveEventStatus.create(eventStatus = input)
            val status2 = LiveEventStatus.create(eventStatus = input)
            val status3 = LiveEventStatus.create(eventStatus = input)

            assertEquals(status1, status2, "Статус для $input должен быть одинаковым (вызов 1-2)")
            assertEquals(status2, status3, "Статус для $input должен быть одинаковым (вызов 2-3)")
            assertEquals(status1, status3, "Статус для $input должен быть одинаковым (вызов 1-3)")
        }
    }

    @Test
    fun `create should handle boundary values correctly`() {
        // Границы валидного диапазона
        val boundaryTests = mapOf(
            -1 to LiveEventStatus.Unknown, // Перед началом валидного диапазона
            0 to LiveEventStatus.Active,   // Начало валидного диапазона
            5 to LiveEventStatus.OnlyExtra, // Конец валидного диапазона  
            6 to LiveEventStatus.Unknown   // После конца валидного диапазона
        )

        boundaryTests.forEach { (input, expected) ->
            val actual = LiveEventStatus.create(eventStatus = input)
            assertEquals(expected, actual, "Граничное значение $input должно возвращать $expected")
        }
    }
}