package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.RecalculateResult
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.repository.ArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.CexPriceRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryCexPriceRepository
import com.arbitrage.scanner.services.ArbitrageFinder
import com.arbitrage.scanner.services.ArbitrageFinderParallelImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты для режима TEST с генерацией mock данных
 *
 * Проверяют, что при использовании WorkMode.TEST:
 * - Генерируются тестовые данные
 * - Данные сохраняются в репозиторий
 * - Находятся арбитражные возможности
 * - Возвращается корректный ответ
 */
class BusinessLogicProcessorImplTestModeTest {

    /**
     * Создает тестовые зависимости с реальными in-memory репозиториями
     */
    private fun createTestDeps(
        cexPriceRepository: CexPriceRepository = InMemoryCexPriceRepository(),
        arbitrageOpportunityRepository: ArbitrageOpportunityRepository = InMemoryArbitrageOpportunityRepository()
    ): BusinessLogicProcessorImplDeps = object : BusinessLogicProcessorImplDeps {
        override val loggerProvider: ArbScanLoggerProvider = ArbScanLoggerProvider()
        override val cexPriceRepository: CexPriceRepository = cexPriceRepository
        override val arbitrageOpportunityRepository: ArbitrageOpportunityRepository = arbitrageOpportunityRepository
        override val arbitrageFinder: ArbitrageFinder = ArbitrageFinderParallelImpl()
    }

    /**
     * Создает контекст для тестирования команды RECALCULATE в режиме TEST
     */
    private fun createTestModeContext(): Context = Context(
        command = Command.RECALCULATE,
        workMode = WorkMode.TEST,
        state = State.NONE
    )

    @Test
    fun `test mode generates mock data and finds opportunities`() = runTest {
        // Given: Пустые репозитории
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // When: Выполняем RECALCULATE в TEST режиме
        val context = createTestModeContext()
        processor.exec(context)

        // Then: Должно быть состояние FINISHING
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")

        // Проверяем, что были сгенерированы цены
        val savedPrices = priceRepo.findAll()
        assertTrue(savedPrices.isNotEmpty(), "Должны быть сгенерированы mock цены")

        // Ожидаем 50 токенов × 5 бирж = 250 записей
        assertEquals(250, savedPrices.size, "Должно быть 250 записей (50 токенов × 5 бирж)")

        // Проверяем, что были найдены и сохранены арбитражные возможности
        val savedOpportunities = opportunityRepo.findAll()
        assertTrue(savedOpportunities.isNotEmpty(), "Должны быть найдены арбитражные возможности")

        // Проверяем, что в ответе есть найденные возможности
        assertTrue(
            context.foundOpportunities.isNotEmpty(),
            "В ответе должны быть арбитражные возможности"
        )
    }

    @Test
    fun `test mode generates deterministic data with seed`() = runTest {
        // Given: Два пустых репозитория
        val priceRepo1 = InMemoryCexPriceRepository()
        val priceRepo2 = InMemoryCexPriceRepository()

        val deps1 = createTestDeps(priceRepo1, InMemoryArbitrageOpportunityRepository())
        val deps2 = createTestDeps(priceRepo2, InMemoryArbitrageOpportunityRepository())

        val processor1 = BusinessLogicProcessorImpl(deps1)
        val processor2 = BusinessLogicProcessorImpl(deps2)

        // When: Выполняем RECALCULATE дважды в TEST режиме
        processor1.exec(createTestModeContext())
        processor2.exec(createTestModeContext())

        // Then: Результаты должны быть идентичными (deterministic generation)
        val prices1 = priceRepo1.findAll()
        val prices2 = priceRepo2.findAll()

        assertEquals(prices1.size, prices2.size, "Количество сгенерированных цен должно совпадать")

        // Проверяем, что цены идентичны
        prices1.zip(prices2).forEach { (price1, price2) ->
            assertEquals(price1.tokenId, price2.tokenId, "TokenId должны совпадать")
            assertEquals(price1.exchangeId, price2.exchangeId, "ExchangeId должны совпадать")
            assertEquals(
                price1.priceRaw.value.doubleValue(exactRequired = false),
                price2.priceRaw.value.doubleValue(exactRequired = false),
                0.0001,
                "Цены должны совпадать"
            )
        }
    }

    @Test
    fun `test mode respects repository isolation`() = runTest {
        // Given: Репозиторий с уже существующими данными
        val priceRepo = InMemoryCexPriceRepository()

        // Добавляем начальные данные
        priceRepo.save(StubsDataFactory.createCexPrice(token = "EXISTING", exchange = "binance", price = 99999.0))
        val initialCount = priceRepo.findAll().size
        assertEquals(1, initialCount, "Начальное количество записей должно быть 1")

        val deps = createTestDeps(priceRepo, InMemoryArbitrageOpportunityRepository())
        val processor = BusinessLogicProcessorImpl(deps)

        // When: Выполняем RECALCULATE в TEST режиме
        processor.exec(createTestModeContext())

        // Then: Данные должны быть объединены (существующие + новые)
        val finalPrices = priceRepo.findAll()
        assertTrue(finalPrices.size > initialCount, "Должны быть добавлены новые записи")

        // Проверяем, что существующие данные сохранились
        val existingPrice = finalPrices.find { it.tokenId.value == "EXISTING" }
        assertTrue(existingPrice != null, "Существующие данные должны сохраниться")
    }

    @Test
    fun `test mode processes full workflow`() = runTest {
        // Given: Чистые репозитории
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // When: Выполняем полный workflow
        val context = createTestModeContext()
        processor.exec(context)

        // Then: Проверяем каждый этап workflow

        // 1. Генерация mock данных
        val generatedPrices = priceRepo.findAll()
        assertEquals(250, generatedPrices.size, "Должно быть сгенерировано 250 цен")

        // 2. Загрузка данных в контекст
        assertEquals(250, context.loadedPrices.size, "Должно быть загружено 250 цен в контекст")

        // 3. Поиск арбитражных возможностей
        assertTrue(context.foundOpportunities.isNotEmpty(), "Должны быть найдены возможности")

        // 4. Сохранение в репозиторий
        val savedOpportunities = opportunityRepo.findAll()
        assertTrue(savedOpportunities.isNotEmpty(), "Возможности должны быть сохранены")

        // 5. Формирование ответа
        assertTrue(context.recalculateResponse != RecalculateResult.DEFAULT, "Ответ должен быть сформирован")

        // Проверяем, что количества совпадают
        assertEquals(
            context.foundOpportunities.size,
            savedOpportunities.size,
            "Количество найденных и сохраненных возможностей должно совпадать"
        )
    }

    @Test
    fun `test mode finds realistic arbitrage opportunities`() = runTest {
        // Given: Репозитории
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // When: Выполняем RECALCULATE
        val context = createTestModeContext()
        processor.exec(context)

        // Then: Проверяем характеристики найденных возможностей
        val opportunities = context.foundOpportunities

        assertTrue(opportunities.isNotEmpty(), "Должны быть найдены возможности")

        // Проверяем, что все возможности имеют положительный спред
        opportunities.forEach { opportunity ->
            assertTrue(opportunity.spread.value > 0.0, "Спред должен быть положительным")
            assertTrue(opportunity.spread.value >= 0.1, "Спред должен быть >= 0.1% (минимальный порог)")
        }

        // Проверяем, что возможности отсортированы по убыванию спреда
        for (i in 0 until opportunities.size - 1) {
            assertTrue(
                opportunities[i].spread.value >= opportunities[i + 1].spread.value,
                "Возможности должны быть отсортированы по убыванию спреда"
            )
        }

        // Проверяем, что биржи покупки и продажи различны
        opportunities.forEach { opportunity ->
            assertTrue(
                opportunity.buyCexExchangeId != opportunity.sellCexExchangeId,
                "Биржи покупки и продажи должны быть разными"
            )
        }
    }
}
