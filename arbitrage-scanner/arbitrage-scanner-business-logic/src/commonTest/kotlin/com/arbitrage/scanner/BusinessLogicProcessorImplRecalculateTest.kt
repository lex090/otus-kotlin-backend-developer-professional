package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.stubs.Stubs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Тесты для команды RECALCULATE в BusinessLogicProcessorImpl
 */
class BusinessLogicProcessorImplRecalculateTest {

    /**
     * Создает тестовые зависимости для BusinessLogicProcessorImpl
     */
    private fun createTestDeps(): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
    }

    /**
     * Создает контекст для тестирования команды RECALCULATE
     */
    private fun createRecalculateContext(
        stubCase: Stubs = Stubs.SUCCESS,
        workMode: WorkMode = WorkMode.STUB,
        state: State = State.NONE,
    ): Context = Context(
        command = Command.RECALCULATE,
        workMode = workMode,
        stubCase = stubCase,
        state = state,
    )

    @Test
    fun `test recalculate with SUCCESS stub returns correct response and FINISHING state`() = runTest {
        // Given: Контекст с командой RECALCULATE и стабом SUCCESS
        val context = createRecalculateContext(
            stubCase = Stubs.SUCCESS,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем результат
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")
        assertNotEquals(
            0,
            context.recalculateResponse.opportunitiesCount,
            "opportunitiesCount должен быть больше 0"
        )
        assertEquals(
            ArbOpStubs.recalculateResult.opportunitiesCount,
            context.recalculateResponse.opportunitiesCount,
            "opportunitiesCount должен совпадать со стабом"
        )
        assertEquals(
            ArbOpStubs.recalculateResult.processingTimeMs,
            context.recalculateResponse.processingTimeMs,
            "processingTimeMs должен совпадать со стабом"
        )
        assertTrue(
            context.errors.isEmpty(),
            "Не должно быть ошибок при успешном выполнении"
        )
    }

    @Test
    fun `test recalculate with NOT_FOUND stub returns validation error`() = runTest {
        // Given: Контекст с командой RECALCULATE и неподдерживаемым стабом NOT_FOUND
        val context = createRecalculateContext(
            stubCase = Stubs.NOT_FOUND,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что возвращается ошибка валидации
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при ошибке")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка"
        )

        val error = context.errors.first()
        assertEquals("validation", error.code, "Код ошибки должен быть 'validation'")
        assertEquals("validation", error.group, "Группа ошибки должна быть 'validation'")
        assertEquals("stub", error.field, "Поле ошибки должно быть 'stub'")
        assertTrue(
            error.message.contains("Wrong stub case is requested"),
            "Сообщение об ошибке должно содержать информацию о неверном стабе"
        )
        assertTrue(
            error.message.contains(Stubs.NOT_FOUND.name),
            "Сообщение об ошибке должно содержать имя запрошенного стаба"
        )
    }

    @Test
    fun `test recalculate with BAD_ID stub returns validation error`() = runTest {
        // Given: Контекст с командой RECALCULATE и неподдерживаемым стабом BAD_ID
        val context = createRecalculateContext(
            stubCase = Stubs.BAD_ID,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что возвращается ошибка валидации
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при ошибке")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка"
        )

        val error = context.errors.first()
        assertEquals("validation", error.code, "Код ошибки должен быть 'validation'")
        assertEquals("validation", error.group, "Группа ошибки должна быть 'validation'")
        assertEquals("stub", error.field, "Поле ошибки должно быть 'stub'")
        assertTrue(
            error.message.contains("Wrong stub case is requested"),
            "Сообщение об ошибке должно содержать информацию о неверном стабе"
        )
    }
}
