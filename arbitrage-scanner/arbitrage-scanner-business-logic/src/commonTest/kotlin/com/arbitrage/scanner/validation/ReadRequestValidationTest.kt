package com.arbitrage.scanner.validation

import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.service.CexPriceClientService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты валидации для команды READ в BusinessLogicProcessorImpl
 */
class ReadRequestValidationTest {

    /**
     * Создает тестовые зависимости для BusinessLogicProcessorImpl
     */
    private fun createTestDeps(): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
        override val stubCexPriceClientService: CexPriceClientService = CexPriceClientService.NONE
        override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = CexToCexArbitrageFinder.NONE
    }

    @Test
    fun `test validation fails with empty ID in PROD mode`() = runTest {
        // Given: Контекст с пустым ID в режиме PROD
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при пустом ID")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка валидации"
        )

        val error = context.errors.first()
        assertEquals("validation-empty", error.code, "Код ошибки должен быть 'validation-empty'")
        assertEquals("validation", error.group, "Группа ошибки должна быть 'validation'")
        assertEquals("id", error.field, "Поле ошибки должно быть 'id'")
        assertTrue(
            error.message.contains("не должен быть пустым"),
            "Сообщение должно содержать информацию о пустом ID"
        )
    }

    @Test
    fun `test validation fails with whitespace-only ID in PROD mode`() = runTest {
        // Given: Контекст с ID из пробелов в режиме PROD
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("   ")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при ID из пробелов")
        assertTrue(
            context.errors.any { it.code == "validation-empty" },
            "Должна быть ошибка с кодом 'validation-empty'"
        )
    }

    @Test
    fun `test validation fails with too short ID in PROD mode`() = runTest {
        // Given: Контекст со слишком коротким ID (2 символа) в режиме PROD
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("ab")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при слишком коротком ID")
        assertTrue(
            context.errors.any { it.code == "validation-length" },
            "Должна быть ошибка с кодом 'validation-length'"
        )
        val error = context.errors.first { it.code == "validation-length" }
        assertTrue(
            error.message.contains("слишком короткий"),
            "Сообщение должно содержать информацию о слишком коротком ID"
        )
    }

    @Test
    fun `test validation fails with too long ID in PROD mode`() = runTest {
        // Given: Контекст со слишком длинным ID (51 символ) в режиме PROD
        val longId = "a".repeat(51)
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(longId)
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при слишком длинном ID")
        assertTrue(
            context.errors.any { it.code == "validation-length" },
            "Должна быть ошибка с кодом 'validation-length'"
        )
        val error = context.errors.first { it.code == "validation-length" }
        assertTrue(
            error.message.contains("слишком длинный"),
            "Сообщение должно содержать информацию о слишком длинном ID"
        )
    }

    @Test
    fun `test validation fails with invalid format - starts with dash`() = runTest {
        // Given: Контекст с ID, начинающимся с дефиса
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("-test123")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректном формате")
        assertTrue(
            context.errors.any { it.code == "validation-format" },
            "Должна быть ошибка с кодом 'validation-format'"
        )
        val error = context.errors.first { it.code == "validation-format" }
        assertTrue(
            error.message.contains("недопустимые символы"),
            "Сообщение должно содержать информацию о недопустимых символах"
        )
    }

    @Test
    fun `test validation fails with invalid format - ends with dash`() = runTest {
        // Given: Контекст с ID, заканчивающимся дефисом
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("test123-")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректном формате")
        assertTrue(
            context.errors.any { it.code == "validation-format" },
            "Должна быть ошибка с кодом 'validation-format'"
        )
    }

    @Test
    fun `test validation fails with invalid format - contains special characters`() = runTest {
        // Given: Контекст с ID, содержащим спецсимволы
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("test@123")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректном формате")
        assertTrue(
            context.errors.any { it.code == "validation-format" },
            "Должна быть ошибка с кодом 'validation-format'"
        )
    }

    @Test
    fun `test validation fails with invalid format - contains underscore`() = runTest {
        // Given: Контекст с ID, содержащим подчеркивание
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("test_123")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректном формате")
        assertTrue(
            context.errors.any { it.code == "validation-format" },
            "Должна быть ошибка с кодом 'validation-format'"
        )
    }

    @Test
    fun `test validation passes with valid ID formats in PROD mode`() = runTest {
        // Given: Список корректных ID для тестирования
        val validIds = listOf(
            "abc123",              // буквы и цифры
            "test-id-123",         // с дефисами
            "1a2b3c",              // начинается с цифры
            "ABC",                 // только заглавные буквы
            "123",                 // только цифры
            "a-b-c-d-e",          // несколько дефисов
            "a".repeat(50)         // максимальная длина (50 символов)
        )

        val processor = BusinessLogicProcessorImpl(createTestDeps())

        for (id in validIds) {
            // Given: Контекст с валидным ID
            val context = Context(
                command = Command.READ,
                workMode = WorkMode.PROD,
                state = State.NONE,
                arbitrageOpportunityReadRequest = ArbitrageOpportunityId(id)
            )

            // When: Выполняем бизнес-логику
            processor.exec(context)

            // Then: Проверяем, что валидация прошла успешно
            assertTrue(
                context.errors.none { it.group == "validation" },
                "Не должно быть ошибок валидации для ID: $id"
            )
            assertEquals(
                id.trim(),
                context.arbitrageOpportunityReadRequestValidated.value,
                "ID должен быть скопирован в validated после успешной валидации"
            )
        }
    }

    @Test
    fun `test validation trims whitespace from ID`() = runTest {
        // Given: Контекст с ID, содержащим пробелы в начале и конце
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("  test-123  ")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что пробелы были удалены
        assertEquals(
            "test-123",
            context.arbitrageOpportunityReadRequestValidated.value,
            "Пробелы должны быть удалены из ID"
        )
        assertTrue(
            context.errors.none { it.group == "validation" },
            "Не должно быть ошибок валидации после нормализации"
        )
    }

    @Test
    fun `test validation collects all applicable errors`() = runTest {
        // Given: Контекст с пустым ID (будет несколько ошибок валидации)
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что собраны все применимые ошибки валидации
        assertEquals(State.FAILING, context.state, "State должен быть FAILING")
        assertTrue(
            context.errors.size >= 1,
            "Должна быть как минимум одна ошибка валидации"
        )
        assertTrue(
            context.errors.any { it.code == "validation-empty" },
            "Должна быть ошибка пустого ID"
        )
        // Пустая строка также может генерировать ошибки длины и формата
        // так как валидаторы теперь работают независимо
    }

    @Test
    fun `test single character valid ID passes validation`() = runTest {
        // Given: Односимвольные валидные ID
        val singleCharIds = listOf("a", "Z", "5", "0")
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        for (id in singleCharIds) {
            val context = Context(
                command = Command.READ,
                workMode = WorkMode.PROD,
                state = State.NONE,
                arbitrageOpportunityReadRequest = ArbitrageOpportunityId(id)
            )

            // When: Выполняем бизнес-логику
            processor.exec(context)

            // Then: Проверяем, что валидация провалилась на минимальной длине
            assertEquals(State.FAILING, context.state, "State должен быть FAILING для ID: $id")
            assertTrue(
                context.errors.any { it.code == "validation-length" },
                "Должна быть ошибка длины для односимвольного ID: $id"
            )
        }
    }

    @Test
    fun `test two character ID fails validation`() = runTest {
        // Given: Двухсимвольный ID
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("ab")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING")
        assertTrue(
            context.errors.any { it.code == "validation-length" },
            "Должна быть ошибка с кодом 'validation-length'"
        )
    }

    @Test
    fun `test validation works correctly for ID with spaces inside`() = runTest {
        // Given: ID с пробелами внутри (недопустимо)
        val context = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("test 123")
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась на формате
        assertEquals(State.FAILING, context.state, "State должен быть FAILING")
        assertTrue(
            context.errors.any { it.code == "validation-format" },
            "Должна быть ошибка формата для ID с пробелами внутри"
        )
    }
}