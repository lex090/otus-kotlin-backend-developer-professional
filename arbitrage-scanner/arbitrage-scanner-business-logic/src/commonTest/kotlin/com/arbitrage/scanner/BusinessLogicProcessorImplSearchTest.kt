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
 * Тесты для команды SEARCH в BusinessLogicProcessorImpl
 */
class BusinessLogicProcessorImplSearchTest {

    /**
     * Создает тестовые зависимости для BusinessLogicProcessorImpl
     */
    private fun createTestDeps(): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
    }

    /**
     * Создает контекст для тестирования команды SEARCH
     */
    private fun createSearchContext(
        stubCase: Stubs = Stubs.SUCCESS,
        workMode: WorkMode = WorkMode.STUB,
        state: State = State.RUNNING,
    ): Context = Context(
        command = Command.SEARCH,
        workMode = workMode,
        stubCase = stubCase,
        state = state,
    )

    @Test
    fun `test search with SUCCESS stub returns list with element and FINISHING state`() = runTest {
        // Given: Контекст с командой SEARCH и стабом SUCCESS
        val context = createSearchContext(
            stubCase = Stubs.SUCCESS,
            workMode = WorkMode.STUB,
            state = State.RUNNING,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем результат
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")
        assertEquals(
            1,
            context.arbitrageOpportunitySearchResponse.size,
            "arbitrageOpportunitySearchResponse должен содержать один элемент"
        )
        assertTrue(
            context.arbitrageOpportunitySearchResponse.contains(ArbOpStubs.arbitrageOpportunity),
            "arbitrageOpportunitySearchResponse должен содержать элемент из ArbOpStubs.arbitrageOpportunity"
        )
        assertTrue(
            context.errors.isEmpty(),
            "Не должно быть ошибок при успешном выполнении"
        )
    }

    @Test
    fun `test search with NOT_FOUND stub returns empty list and FINISHING state`() = runTest {
        // Given: Контекст с командой SEARCH и стабом NOT_FOUND
        val context = createSearchContext(
            stubCase = Stubs.NOT_FOUND,
            workMode = WorkMode.STUB,
            state = State.RUNNING,
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что возвращается пустой список
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")
        assertTrue(
            context.arbitrageOpportunitySearchResponse.isEmpty(),
            "arbitrageOpportunitySearchResponse должен быть пустым"
        )
        assertEquals(
            0,
            context.arbitrageOpportunitySearchResponse.size,
            "Размер arbitrageOpportunitySearchResponse должен быть 0"
        )
        assertTrue(
            context.errors.isEmpty(),
            "Не должно быть ошибок при NOT_FOUND стабе"
        )
    }

    @Test
    fun `test search with unsupported stub BAD_ID returns validation error`() = runTest {
        // Given: Контекст с командой SEARCH и неподдерживаемым стабом BAD_ID
        // Стаб BAD_ID не поддерживается для команды SEARCH
        val context = createSearchContext(
            stubCase = Stubs.BAD_ID,
            workMode = WorkMode.STUB,
            state = State.RUNNING,
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
            error.message.contains(Stubs.BAD_ID.name),
            "Сообщение об ошибке должно содержать имя запрошенного стаба"
        )
    }
}
