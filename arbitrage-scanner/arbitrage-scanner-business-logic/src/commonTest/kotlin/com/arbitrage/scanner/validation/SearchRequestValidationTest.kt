package com.arbitrage.scanner.validation

import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexExchangeIds
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexTokenIdsFilter
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты валидации для команды SEARCH в BusinessLogicProcessorImpl
 */
class SearchRequestValidationTest {

    /**
     * Создает тестовые зависимости для BusinessLogicProcessorImpl
     */
    private fun createTestDeps(): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
        override val cexToCexArbitrageFinder: CexToCexArbitrageFinder = CexToCexArbitrageFinder.NONE
        override val prodCexPriceClientService: CexPriceClientService = CexPriceClientService.NONE
        override val testCexPriceClientService: CexPriceClientService = CexPriceClientService.NONE
        override val stubCexPriceClientService: CexPriceClientService = CexPriceClientService.NONE
        override val prodArbOpRepository: IArbOpRepository = InMemoryArbOpRepository()
        override val stubArbOpRepository: IArbOpRepository = InMemoryArbOpRepository()
        override val testArbOpRepository: IArbOpRepository = InMemoryArbOpRepository()
    }

    @Test
    fun `test validation fails with invalid token ID format`() = runTest {
        // Given: Контекст с некорректным ID токена
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(
                    CexTokenId("B"),
                    CexTokenId("test@token"),
                    CexTokenId("-invalid"),
                    CexTokenId("valid-token")
                )),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet())
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректных ID токенов")
        assertTrue(
            context.internalErrors.any { it.code == "validation-format" && it.field == "cexTokenIds" },
            "Должна быть ошибка формата для cexTokenIds"
        )
    }

    @Test
    fun `test validation fails with invalid buy exchange ID format`() = runTest {
        // Given: Контекст с некорректным ID биржи покупки
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter.NONE,
                buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("_bybit"))),
                sellExchangeIds = CexExchangeIds(setOf(CexExchangeId("binance")))
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректном ID биржи покупки")
        assertTrue(
            context.internalErrors.any { it.code == "validation-format" && it.field == "buyExchangeIds" },
            "Должна быть ошибка формата для buyExchangeIds"
        )
    }

    @Test
    fun `test validation fails with negative minSpread`() = runTest {
        // Given: Контекст с отрицательным minSpread
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet()),
                minSpread = ArbitrageOpportunitySpread(-5.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при отрицательном minSpread")
        assertTrue(
            context.internalErrors.any { it.code == "validation-range" && it.field == "minSpread" },
            "Должна быть ошибка диапазона для minSpread"
        )
        val error = context.internalErrors.first { it.field == "minSpread" }
        assertTrue(
            error.message.contains("не может быть отрицательным"),
            "Сообщение должно содержать информацию об отрицательном спреде"
        )
    }

    @Test
    fun `test validation passes with valid filter - only minSpread`() = runTest {
        // Given: Контекст с валидным фильтром (только minSpread)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter.NONE,
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet()),
                minSpread = ArbitrageOpportunitySpread(5.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.internalErrors.none { it.group == "validation" },
            "Не должно быть ошибок валидации для валидного фильтра"
        )
        assertEquals(
            5.0,
            context.arbitrageOpportunitySearchRequestValidated.minSpread?.value,
            "minSpread должен быть скопирован в validated после успешной валидации"
        )
    }

    @Test
    fun `test validation passes with complex valid filter`() = runTest {
        // Given: Контекст со сложным валидным фильтром
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"), CexTokenId("ETH"), CexTokenId("USDT"))),
                buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("binance"))),
                sellExchangeIds = CexExchangeIds(setOf(CexExchangeId("okx"))),
                minSpread = ArbitrageOpportunitySpread(2.5),
                maxSpread = ArbitrageOpportunitySpread(10.0),
                status = ArbitrageOpportunityStatus.ACTIVE
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.internalErrors.none { it.group == "validation" },
            "Не должно быть ошибок валидации для сложного валидного фильтра"
        )
        assertEquals(
            3,
            context.arbitrageOpportunitySearchRequestValidated.cexTokenIdsFilter.value.size,
            "Все CEX токены должны быть скопированы в validated"
        )
        assertEquals(
            CexExchangeIds(setOf(CexExchangeId("binance"))),
            context.arbitrageOpportunitySearchRequestValidated.buyExchangeIds,
            "buyExchangeIds должен быть скопирован в validated"
        )
        assertEquals(
            CexExchangeIds(setOf(CexExchangeId("okx"))),
            context.arbitrageOpportunitySearchRequestValidated.sellExchangeIds,
            "sellExchangeIds должен быть скопирован в validated"
        )
    }

    @Test
    fun `test validation trims whitespace from IDs`() = runTest {
        // Given: Контекст с ID, содержащими пробелы
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("  BTC  "), CexTokenId(" ETH"), CexTokenId("USDT  "))),
                buyExchangeIds = CexExchangeIds(setOf(CexExchangeId(" binance "))),
                sellExchangeIds = CexExchangeIds(setOf(CexExchangeId("  okx")))
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что пробелы были удалены
        val validated = context.arbitrageOpportunitySearchRequestValidated
        assertTrue(
            validated.cexTokenIdsFilter.value.contains(CexTokenId("BTC")),
            "Пробелы должны быть удалены из CEX token IDs"
        )
        assertTrue(
            validated.cexTokenIdsFilter.value.contains(CexTokenId("ETH")),
            "Пробелы должны быть удалены из CEX token IDs"
        )
        assertTrue(
            validated.cexTokenIdsFilter.value.contains(CexTokenId("USDT")),
            "Пробелы должны быть удалены из CEX token IDs"
        )
        assertTrue(
            validated.buyExchangeIds.value.contains(CexExchangeId("binance")),
            "Пробелы должны быть удалены из buyExchangeIds"
        )
        assertTrue(
            validated.sellExchangeIds.value.contains(CexExchangeId("okx")),
            "Пробелы должны быть удалены из sellExchangeIds"
        )
    }

    @Test
    fun `test validation collects multiple errors`() = runTest {
        // Given: Контекст с множественными ошибками валидации
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("B"))), // Слишком короткий
                buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("ok"))), // Слишком короткий
                sellExchangeIds = CexExchangeIds(emptySet()),
                minSpread = ArbitrageOpportunitySpread(-10.0) // Отрицательный
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что собраны все ошибки
        assertEquals(State.FAILING, context.state, "State должен быть FAILING")
        assertTrue(
            context.internalErrors.size >= 3,
            "Должно быть минимум 3 ошибки валидации"
        )
        assertTrue(
            context.internalErrors.any { it.field == "cexTokenIds" },
            "Должна быть ошибка для cexTokenIds"
        )
        assertTrue(
            context.internalErrors.any { it.field == "buyExchangeIds" },
            "Должна быть ошибка для buyExchangeIds"
        )
        assertTrue(
            context.internalErrors.any { it.field == "minSpread" },
            "Должна быть ошибка для minSpread"
        )
    }

    @Test
    fun `test validation with edge case - minSpread zero is valid`() = runTest {
        // Given: Контекст с minSpread равным нулю (должен быть валидным)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet()),
                minSpread = ArbitrageOpportunitySpread(0.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.internalErrors.none { it.field == "minSpread" },
            "Не должно быть ошибок для minSpread равного нулю"
        )
    }

    @Test
    fun `test validation with edge case - maxSpread 100 is valid`() = runTest {
        // Given: Контекст с maxSpread равным 100 (максимальное валидное значение)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet()),
                maxSpread = ArbitrageOpportunitySpread(100.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.internalErrors.none { it.field == "maxSpread" },
            "Не должно быть ошибок для maxSpread равного 100"
        )
    }

    @Test
    fun `test validation with mixed valid and invalid token IDs`() = runTest {
        // Given: Контекст со смешанными валидными и невалидными ID токенов
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf(
                    CexTokenId("BTC"),      // Валидный
                    CexTokenId("B"),         // Слишком короткий
                    CexTokenId("ETH-USDT"), // Валидный с дефисом
                    CexTokenId("@INVALID")   // Начинается со спецсимвола
                )),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet())
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при наличии невалидных токенов")
        assertTrue(
            context.internalErrors.any { it.field == "cexTokenIds" },
            "Должна быть ошибка для cexTokenIds"
        )
    }

    @Test
    fun `test validation with invalid sellExchangeId format`() = runTest {
        // Given: Контекст с невалидным sellExchangeId
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter.NONE,
                buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("binance"))),
                sellExchangeIds = CexExchangeIds(setOf(CexExchangeId("-bybit"))) // Начинается с дефиса
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при невалидном sellExchangeId")
        assertTrue(
            context.internalErrors.any { it.field == "sellExchangeIds" },
            "Должна быть ошибка для sellExchangeIds"
        )
    }
}