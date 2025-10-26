package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.models.*
import com.arbitrage.scanner.repository.ArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.CexPriceRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryArbitrageOpportunityRepository
import com.arbitrage.scanner.repository.inmemory.InMemoryCexPriceRepository
import com.arbitrage.scanner.services.ArbitrageFinder
import com.arbitrage.scanner.services.ArbitrageFinderParallelImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Интеграционные тесты для проверки lifecycle управления арбитражными возможностями
 * при множественных вызовах RECALCULATE
 *
 * Проверяет корректность работы алгоритма сравнения возможностей:
 * - Новые возможности создаются только если их не было раньше
 * - Существующие возможности остаются активными
 * - Исчезнувшие возможности помечаются как завершенные
 */
class BusinessLogicProcessorImplRecalculateLifecycleTest {

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
     * Создает контекст для тестирования команды RECALCULATE
     */
    private fun createRecalculateContext(): Context = Context(
        command = Command.RECALCULATE,
        workMode = WorkMode.PROD,
        state = State.NONE
    )

    @Test
    fun `test first recalculate creates new opportunities`() = runTest {
        // Given: Пустые репозитории
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        // Добавляем цены с арбитражной возможностью
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0)) // 5% спред

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // When: Выполняем первый RECALCULATE
        val context = createRecalculateContext()
        processor.exec(context)

        // Then: Должна быть создана одна возможность
        assertEquals(State.FINISHING, context.state, "State должен быть FINISHING")
        val active = opportunityRepo.findActive()
        assertEquals(1, active.size, "Должна быть создана 1 активная возможность")

        val opportunity = active.first()
        assertEquals(CexTokenId("BTC"), opportunity.cexTokenId)
        assertEquals(CexExchangeId("binance"), opportunity.buyCexExchangeId)
        assertEquals(CexExchangeId("okx"), opportunity.sellCexExchangeId)
        assertNotNull(opportunity.startTimestamp, "startTimestamp должен быть установлен")
        assertEquals(null, opportunity.endTimestamp, "endTimestamp должен быть null")
    }

    @Test
    fun `test second recalculate with same opportunities does not create duplicates`() = runTest {
        // Given: Репозитории с одной активной возможностью
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0))

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // Первый RECALCULATE
        processor.exec(createRecalculateContext())

        val activeAfterFirst = opportunityRepo.findActive()
        assertEquals(1, activeAfterFirst.size, "После первого RECALCULATE должна быть 1 возможность")
        val firstOpportunityId = activeAfterFirst.first().id

        // When: Выполняем второй RECALCULATE с теми же ценами (та же возможность)
        processor.exec(createRecalculateContext())

        // Then: Не должно быть создано новых возможностей, старая должна остаться активной
        val activeAfterSecond = opportunityRepo.findActive()
        assertEquals(1, activeAfterSecond.size, "Должна остаться 1 активная возможность (без дубликатов)")

        val secondOpportunity = activeAfterSecond.first()
        assertEquals(firstOpportunityId, secondOpportunity.id, "ID должен остаться тем же (не создана новая запись)")
        assertEquals(null, secondOpportunity.endTimestamp, "Возможность должна остаться активной")

        // Всего записей в репозитории должна быть только одна
        val allOpportunities = opportunityRepo.findAll()
        assertEquals(1, allOpportunities.size, "В репозитории должна быть только 1 запись (без дубликатов)")
    }

    @Test
    fun `test third recalculate with different opportunities marks old as ended`() = runTest {
        // Given: Репозитории с одной активной возможностью
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        // Первоначальные цены: BTC на binance/okx
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0))

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // Первый RECALCULATE
        processor.exec(createRecalculateContext())

        val activeAfterFirst = opportunityRepo.findActive()
        assertEquals(1, activeAfterFirst.size)
        val firstOpportunityId = activeAfterFirst.first().id

        // When: Изменяем цены - теперь арбитраж на ETH между другими биржами
        priceRepo.clear()
        priceRepo.save(StubsDataFactory.createCexPrice(token = "ETH", exchange = "bybit", price = 200.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 210.0)) // 5% спред

        processor.exec(createRecalculateContext())

        // Then: Старая возможность должна быть помечена как завершенная
        val oldOpportunity = opportunityRepo.findById(firstOpportunityId)
        assertNotNull(oldOpportunity, "Старая возможность должна существовать")
        assertNotNull(oldOpportunity.endTimestamp, "Старая возможность должна быть помечена как завершенная")

        // Новая возможность должна быть создана и активна
        val activeAfterThird = opportunityRepo.findActive()
        assertEquals(1, activeAfterThird.size, "Должна быть 1 новая активная возможность")

        val newOpportunity = activeAfterThird.first()
        assertEquals(CexTokenId("ETH"), newOpportunity.cexTokenId)
        assertEquals(CexExchangeId("bybit"), newOpportunity.buyCexExchangeId)
        assertEquals(CexExchangeId("okx"), newOpportunity.sellCexExchangeId)
        assertEquals(null, newOpportunity.endTimestamp, "Новая возможность должна быть активной")

        // Всего записей должно быть 2: одна завершенная, одна активная
        val allOpportunities = opportunityRepo.findAll()
        assertEquals(2, allOpportunities.size, "Должно быть 2 записи: старая (завершенная) и новая (активная)")
    }

    @Test
    fun `test recalculate with partial overlap keeps existing and creates new`() = runTest {
        // Given: Репозитории с двумя активными возможностями
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        // Первоначальные цены: BTC и ETH с арбитражем
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "ETH", exchange = "bybit", price = 200.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 210.0))

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // Первый RECALCULATE
        processor.exec(createRecalculateContext())

        val activeAfterFirst = opportunityRepo.findActive()
        assertEquals(2, activeAfterFirst.size, "Должно быть 2 активных возможности")

        val btcOpportunity = activeAfterFirst.first { it.cexTokenId.value == "BTC" }
        val ethOpportunity = activeAfterFirst.first { it.cexTokenId.value == "ETH" }

        // When: Изменяем цены - BTC остается, ETH исчезает, USDT появляется
        priceRepo.clear()
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0))  // BTC остается
        priceRepo.save(StubsDataFactory.createCexPrice(token = "USDT", exchange = "binance", price = 1.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "USDT", exchange = "bybit", price = 1.05)) // USDT появляется

        processor.exec(createRecalculateContext())

        // Then: BTC должен остаться активным (тот же ID)
        val btcAfterSecond = opportunityRepo.findById(btcOpportunity.id)
        assertNotNull(btcAfterSecond, "BTC возможность должна существовать")
        assertEquals(null, btcAfterSecond.endTimestamp, "BTC возможность должна остаться активной")

        // ETH должен быть помечен как завершенный
        val ethAfterSecond = opportunityRepo.findById(ethOpportunity.id)
        assertNotNull(ethAfterSecond, "ETH возможность должна существовать")
        assertNotNull(ethAfterSecond.endTimestamp, "ETH возможность должна быть завершена")

        // USDT должен быть создан и активен
        val activeAfterSecond = opportunityRepo.findActive()
        assertEquals(2, activeAfterSecond.size, "Должно быть 2 активных: BTC (старый) и USDT (новый)")

        val usdtOpportunity = activeAfterSecond.firstOrNull { it.cexTokenId.value == "USDT" }
        assertNotNull(usdtOpportunity, "USDT возможность должна быть создана")
        assertEquals(null, usdtOpportunity.endTimestamp, "USDT возможность должна быть активной")

        // Всего записей: 3 (BTC активен, ETH завершен, USDT активен)
        val allOpportunities = opportunityRepo.findAll()
        assertEquals(3, allOpportunities.size, "Должно быть 3 записи: BTC, ETH, USDT")
    }

    @Test
    fun `test recalculate with no opportunities marks all as ended`() = runTest {
        // Given: Репозитории с активными возможностями
        val priceRepo = InMemoryCexPriceRepository()
        val opportunityRepo = InMemoryArbitrageOpportunityRepository()

        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0))
        priceRepo.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0))

        val deps = createTestDeps(priceRepo, opportunityRepo)
        val processor = BusinessLogicProcessorImpl(deps)

        // Первый RECALCULATE
        processor.exec(createRecalculateContext())

        assertEquals(1, opportunityRepo.findActive().size, "Должна быть 1 активная возможность")
        val opportunityId = opportunityRepo.findActive().first().id

        // When: Удаляем все цены (нет арбитражных возможностей)
        priceRepo.clear()
        processor.exec(createRecalculateContext())

        // Then: Все возможности должны быть помечены как завершенные
        val activeAfterSecond = opportunityRepo.findActive()
        assertTrue(activeAfterSecond.isEmpty(), "Не должно быть активных возможностей")

        val endedOpportunity = opportunityRepo.findById(opportunityId)
        assertNotNull(endedOpportunity, "Возможность должна существовать")
        assertNotNull(endedOpportunity.endTimestamp, "Возможность должна быть помечена как завершенная")
    }
}
