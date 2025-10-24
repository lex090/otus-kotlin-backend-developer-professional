package com.arbitrage.scanner.validation

import com.arbitrage.scanner.BusinessLogicProcessorImpl
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.DexChainId
import com.arbitrage.scanner.models.DexExchangeId
import com.arbitrage.scanner.models.DexTokenId
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
    }

    @Test
    fun `test validation fails with completely empty filter in PROD mode`() = runTest {
        // Given: Контекст с пустым фильтром в режиме PROD
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при пустом фильтре")
        assertTrue(
            context.errors.isNotEmpty(),
            "Должна быть хотя бы одна ошибка валидации"
        )

        val error = context.errors.first()
        assertEquals("validation-empty", error.code, "Код ошибки должен быть 'validation-empty'")
        assertEquals("validation", error.group, "Группа ошибки должна быть 'validation'")
        assertEquals("filter", error.field, "Поле ошибки должно быть 'filter'")
        assertTrue(
            error.message.contains("не должен быть пустым"),
            "Сообщение должно содержать информацию о пустом фильтре"
        )
    }

    @Test
    fun `test validation fails with invalid token ID format`() = runTest {
        // Given: Контекст с некорректным ID токена
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("B"), DexTokenId("test@token"), DexTokenId("valid-token")),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = setOf(CexTokenId("-invalid")),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread.DEFAULT
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректных ID токенов")
        assertTrue(
            context.errors.any { it.code == "validation-format" && (it.field == "dexTokenIds" || it.field == "cexTokenIds") },
            "Должна быть ошибка формата для dexTokenIds или cexTokenIds"
        )
    }

    @Test
    fun `test validation fails with invalid exchange ID format`() = runTest {
        // Given: Контекст с некорректным ID биржи
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = setOf(DexExchangeId("ok"), DexExchangeId("binance-"), DexExchangeId("valid")),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = setOf(CexExchangeId("_bybit")),
                spread = ArbitrageOpportunitySpread(1.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректных ID бирж")
        assertTrue(
            context.errors.any { it.code == "validation-format" && (it.field == "dexExchangeIds" || it.field == "cexExchangeIds") },
            "Должна быть ошибка формата для dexExchangeIds или cexExchangeIds"
        )
    }

    @Test
    fun `test validation fails with invalid chain ID format`() = runTest {
        // Given: Контекст с некорректным ID блокчейна
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = emptySet(),
                dexChainIds = setOf(DexChainId("b"), DexChainId("eth@mainnet"), DexChainId("-polygon")),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(1.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при некорректных ID блокчейнов")
        assertTrue(
            context.errors.any { it.code == "validation-format" && it.field == "chainIds" },
            "Должна быть ошибка формата для chainIds"
        )
    }

    @Test
    fun `test validation fails with negative spread`() = runTest {
        // Given: Контекст с отрицательным спредом
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("BTC")),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(-5.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при отрицательном спреде")
        assertTrue(
            context.errors.any { it.code == "validation-range" && it.field == "spread" },
            "Должна быть ошибка диапазона для spread"
        )
        val error = context.errors.first { it.field == "spread" }
        assertTrue(
            error.message.contains("не может быть отрицательным"),
            "Сообщение должно содержать информацию об отрицательном спреде"
        )
    }

    @Test
    fun `test validation fails with too large spread`() = runTest {
        // Given: Контекст со слишком большим спредом
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("BTC")),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(150.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация провалилась
        assertEquals(State.FAILING, context.state, "State должен быть FAILING при слишком большом спреде")
        assertTrue(
            context.errors.any { it.code == "validation-range" && it.field == "spread" },
            "Должна быть ошибка диапазона для spread"
        )
        val error = context.errors.first { it.field == "spread" }
        assertTrue(
            error.message.contains("слишком большой"),
            "Сообщение должно содержать информацию о слишком большом спреде"
        )
    }

    @Test
    fun `test validation passes with valid filter - only spread`() = runTest {
        // Given: Контекст с валидным фильтром (только спред)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(5.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.errors.none { it.group == "validation" },
            "Не должно быть ошибок валидации для валидного фильтра"
        )
        assertEquals(
            5.0,
            context.arbitrageOpportunitySearchRequestValidated.spread.value,
            "Спред должен быть скопирован в validated после успешной валидации"
        )
    }

    @Test
    fun `test validation passes with complex valid filter`() = runTest {
        // Given: Контекст со сложным валидным фильтром
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("BTC"), DexTokenId("ETH"), DexTokenId("BNB-USDT")),
                dexExchangeIds = setOf(DexExchangeId("pancakeswap"), DexExchangeId("uniswap-v3")),
                dexChainIds = setOf(DexChainId("bsc"), DexChainId("ethereum")),
                cexTokenIds = setOf(CexTokenId("BTC"), CexTokenId("USDT")),
                cexExchangeIds = setOf(CexExchangeId("binance"), CexExchangeId("okx")),
                spread = ArbitrageOpportunitySpread(2.5)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.errors.none { it.group == "validation" },
            "Не должно быть ошибок валидации для сложного валидного фильтра"
        )
        assertEquals(
            3,
            context.arbitrageOpportunitySearchRequestValidated.dexTokenIds.size,
            "Все DEX токены должны быть скопированы в validated"
        )
        assertEquals(
            2,
            context.arbitrageOpportunitySearchRequestValidated.dexExchangeIds.size,
            "Все DEX биржи должны быть скопированы в validated"
        )
    }

    @Test
    fun `test validation trims whitespace from IDs`() = runTest {
        // Given: Контекст с ID, содержащими пробелы
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("  BTC  "), DexTokenId(" ETH")),
                dexExchangeIds = setOf(DexExchangeId(" pancakeswap ")),
                dexChainIds = setOf(DexChainId("  bsc  ")),
                cexTokenIds = setOf(CexTokenId("USDT  ")),
                cexExchangeIds = setOf(CexExchangeId("  binance")),
                spread = ArbitrageOpportunitySpread(1.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что пробелы были удалены
        val validated = context.arbitrageOpportunitySearchRequestValidated
        assertTrue(
            validated.dexTokenIds.contains(DexTokenId("BTC")),
            "Пробелы должны быть удалены из DEX token IDs"
        )
        assertTrue(
            validated.dexTokenIds.contains(DexTokenId("ETH")),
            "Пробелы должны быть удалены из DEX token IDs"
        )
        assertTrue(
            validated.dexExchangeIds.contains(DexExchangeId("pancakeswap")),
            "Пробелы должны быть удалены из DEX exchange IDs"
        )
        assertTrue(
            validated.dexChainIds.contains(DexChainId("bsc")),
            "Пробелы должны быть удалены из DEX chain IDs"
        )
        assertTrue(
            validated.cexTokenIds.contains(CexTokenId("USDT")),
            "Пробелы должны быть удалены из CEX token IDs"
        )
        assertTrue(
            validated.cexExchangeIds.contains(CexExchangeId("binance")),
            "Пробелы должны быть удалены из CEX exchange IDs"
        )
    }

    @Test
    fun `test validation collects multiple errors`() = runTest {
        // Given: Контекст с множественными ошибками валидации
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("B")), // Слишком короткий
                dexExchangeIds = setOf(DexExchangeId("ok")), // Слишком короткий
                dexChainIds = setOf(DexChainId("-invalid")), // Некорректный формат
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(-10.0) // Отрицательный
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что собраны все ошибки
        assertEquals(State.FAILING, context.state, "State должен быть FAILING")
        assertTrue(
            context.errors.size >= 3,
            "Должно быть минимум 3 ошибки валидации"
        )
        assertTrue(
            context.errors.any { it.field == "dexTokenIds" || it.field == "cexTokenIds" },
            "Должна быть ошибка для dexTokenIds или cexTokenIds"
        )
        assertTrue(
            context.errors.any { it.field == "dexExchangeIds" || it.field == "cexExchangeIds" },
            "Должна быть ошибка для dexExchangeIds или cexExchangeIds"
        )
        assertTrue(
            context.errors.any { it.field == "chainIds" },
            "Должна быть ошибка для chainIds"
        )
        assertTrue(
            context.errors.any { it.field == "spread" },
            "Должна быть ошибка для spread"
        )
    }

    @Test
    fun `test validation with edge case - spread zero is valid`() = runTest {
        // Given: Контекст со спредом равным нулю (должен быть валидным)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("BTC")),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(0.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.errors.none { it.field == "spread" },
            "Не должно быть ошибок для спреда равного нулю"
        )
    }

    @Test
    fun `test validation with edge case - spread 100 is valid`() = runTest {
        // Given: Контекст со спредом равным 100 (максимальное валидное значение)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("BTC")),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(100.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.errors.none { it.field == "spread" },
            "Не должно быть ошибок для спреда равного 100"
        )
    }

    @Test
    fun `test validation with numeric exchange ID like 1inch`() = runTest {
        // Given: Контекст с биржей, начинающейся с цифры (валидно для бирж)
        val context = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = setOf(DexExchangeId("1inch")),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = ArbitrageOpportunitySpread(1.0)
            )
        )
        val processor = BusinessLogicProcessorImpl(createTestDeps())

        // When: Выполняем бизнес-логику
        processor.exec(context)

        // Then: Проверяем, что валидация прошла успешно
        assertTrue(
            context.errors.none { it.field == "dexExchangeIds" || it.field == "cexExchangeIds" },
            "Не должно быть ошибок для биржи '1inch'"
        )
    }
}