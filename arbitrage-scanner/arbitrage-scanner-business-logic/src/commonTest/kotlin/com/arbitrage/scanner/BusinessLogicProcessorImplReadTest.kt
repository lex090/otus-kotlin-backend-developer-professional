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
import kotlin.test.assertTrue

/**
 * Тесты для команды READ в BusinessLogicProcessorImpl
 */
class BusinessLogicProcessorImplReadTest {

    /**
     * Создает тестовые зависимости для BusinessLogicProcessorImpl
     */
    private fun createTestDeps(): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
    }

    /**
     * Создает контекст для тестирования команды READ
     */
    private fun createReadContext(
        stubCase: Stubs = Stubs.SUCCESS,
        workMode: WorkMode = WorkMode.STUB,
        state: State = State.NONE,
    ): Context = Context(
        command = Command.READ,
        workMode = workMode,
        stubCase = stubCase,
        state = state,
    )

    @Test
    fun `test read with SUCCESS stub returns correct response and FINISHING state`() = runTest {
        // Given: Контекст с командой READ и стабом SUCCESS
        val context = createReadContext(
            stubCase = Stubs.SUCCESS,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем результат
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")
        assertEquals(
            ArbOpStubs.arbitrageOpportunity,
            context.arbitrageOpportunityReadResponse,
            "arbitrageOpportunityReadResponse должен совпадать со стабом"
        )
        assertEquals(
            ArbOpStubs.arbitrageOpportunity.id,
            context.arbitrageOpportunityReadResponse.id,
            "ID арбитражной возможности должен совпадать со стабом"
        )
        assertTrue(
            context.errors.isEmpty(),
            "Не должно быть ошибок при успешном выполнении"
        )
    }

    @Test
    fun `test read with NOT_FOUND stub returns error and FAILING state`() = runTest {
        // Given: Контекст с командой READ и стабом NOT_FOUND
        val context = createReadContext(
            stubCase = Stubs.NOT_FOUND,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что возвращается ошибка not-found
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при ошибке")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка"
        )

        val error = context.errors.first()
        assertEquals("not-found", error.code, "Код ошибки должен быть 'not-found'")
        assertEquals("stub", error.group, "Группа ошибки должна быть 'stub'")
        assertEquals("id", error.field, "Поле ошибки должно быть 'id'")
        assertTrue(
            error.message.contains("Арбитражная возможность не найдена"),
            "Сообщение об ошибке должно содержать информацию о том, что возможность не найдена"
        )
    }

    @Test
    fun `test read with BAD_ID stub returns error and FAILING state`() = runTest {
        // Given: Контекст с командой READ и стабом BAD_ID
        val context = createReadContext(
            stubCase = Stubs.BAD_ID,
            workMode = WorkMode.STUB,
            state = State.NONE,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что возвращается ошибка bad-id
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при ошибке")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка"
        )

        val error = context.errors.first()
        assertEquals("bad-id", error.code, "Код ошибки должен быть 'bad-id'")
        assertEquals("stub", error.group, "Группа ошибки должна быть 'stub'")
        assertEquals("id", error.field, "Поле ошибки должно быть 'id'")
        assertTrue(
            error.message.contains("Некорректный идентификатор арбитражной возможности"),
            "Сообщение об ошибке должно содержать информацию о некорректном идентификаторе"
        )
    }
}
