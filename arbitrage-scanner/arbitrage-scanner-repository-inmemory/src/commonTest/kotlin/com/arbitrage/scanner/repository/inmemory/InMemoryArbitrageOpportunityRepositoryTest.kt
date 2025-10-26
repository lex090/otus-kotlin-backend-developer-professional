package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit тесты для InMemoryArbitrageOpportunityRepository (T023)
 *
 * Проверяет CRUD операции и особенности:
 * - Auto-ID generation: автоматическая генерация ID при save
 * - save: сохранение одной возможности
 * - saveAll: массовое сохранение с автоматической генерацией ID
 * - findById: поиск по ID
 * - findAll: получение всех возможностей
 * - findActive: получение только активных (endTimestamp == null)
 * - markAsEnded: установка endTimestamp
 * - clear: очистка репозитория
 */
class InMemoryArbitrageOpportunityRepositoryTest {

    @Test
    fun `test save generates auto ID when DEFAULT`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Сохраняем возможность без ID (DEFAULT)
        val opportunity = StubsDataFactory.createArbitrageOpportunity()
        val generatedId = repository.save(opportunity)

        // Then: Должен быть сгенерирован ID
        assertTrue(generatedId.value.isNotEmpty(), "ID должен быть сгенерирован")
        assertTrue(generatedId.value.startsWith("arb-"), "ID должен начинаться с 'arb-'")
    }

    @Test
    fun `test save generates sequential IDs`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Сохраняем три возможности
        val id1 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))
        val id2 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH"))
        val id3 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT"))

        // Then: ID должны быть последовательными
        assertEquals("arb-1", id1.value, "Первый ID должен быть arb-1")
        assertEquals("arb-2", id2.value, "Второй ID должен быть arb-2")
        assertEquals("arb-3", id3.value, "Третий ID должен быть arb-3")
    }

    @Test
    fun `test save uses provided ID if not DEFAULT`() = runTest {
        // Given: Репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Сохраняем возможность с явно заданным ID
        val customId = "custom-123"
        val opportunity = StubsDataFactory.createArbitrageOpportunity(id = customId)
        val savedId = repository.save(opportunity)

        // Then: Должен использоваться предоставленный ID
        assertEquals(customId, savedId.value, "Должен использоваться предоставленный ID")
    }

    @Test
    fun `test saveAll generates IDs for all opportunities`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Массово сохраняем возможности без ID
        val opportunities = listOf(
            StubsDataFactory.createArbitrageOpportunity(token = "BTC"),
            StubsDataFactory.createArbitrageOpportunity(token = "ETH"),
            StubsDataFactory.createArbitrageOpportunity(token = "USDT")
        )
        val generatedIds = repository.saveAll(opportunities)

        // Then: Должны быть сгенерированы ID для всех
        assertEquals(3, generatedIds.size, "Должно быть 3 сгенерированных ID")
        assertEquals("arb-1", generatedIds[0].value, "Первый ID")
        assertEquals("arb-2", generatedIds[1].value, "Второй ID")
        assertEquals("arb-3", generatedIds[2].value, "Третий ID")
    }

    @Test
    fun `test findById returns correct opportunity`() = runTest {
        // Given: Репозиторий с несколькими возможностями
        val repository = InMemoryArbitrageOpportunityRepository()
        val id1 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))
        val id2 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH"))
        val id3 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT"))

        // When: Ищем по ID
        val found = repository.findById(id2)

        // Then: Должна вернуться правильная возможность
        assertNotNull(found, "Возможность должна быть найдена")
        assertEquals("ETH", found.cexTokenId.value, "Должна вернуться возможность для ETH")
        assertEquals(id2, found.id, "ID должен совпадать")
    }

    @Test
    fun `test findById returns null for non-existent ID`() = runTest {
        // Given: Репозиторий с одной возможностью
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))

        // When: Ищем несуществующий ID
        val found = repository.findById(ArbitrageOpportunityId("non-existent"))

        // Then: Должен вернуться null
        assertNull(found, "Для несуществующего ID должен вернуться null")
    }

    @Test
    fun `test findAll returns all opportunities`() = runTest {
        // Given: Репозиторий с несколькими возможностями
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH"))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT"))

        // When: Получаем все возможности
        val all = repository.findAll()

        // Then: Должны вернуться все 3 возможности
        assertEquals(3, all.size, "Должны вернуться все 3 возможности")
    }

    @Test
    fun `test findAll returns empty list when repository is empty`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Получаем все возможности
        val all = repository.findAll()

        // Then: Должен вернуться пустой список
        assertTrue(all.isEmpty(), "Для пустого репозитория должен вернуться пустой список")
    }

    @Test
    fun `test findActive returns only opportunities with null endTimestamp`() = runTest {
        // Given: Репозиторий с активными и завершенными возможностями
        val repository = InMemoryArbitrageOpportunityRepository()

        // Активные возможности (endTimestamp == null)
        val id1 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC", endTimestamp = null))
        val id2 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH", endTimestamp = null))

        // Завершенная возможность (endTimestamp установлен)
        val id3 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT", endTimestamp = 1640995300))

        // When: Получаем только активные
        val active = repository.findActive()

        // Then: Должны вернуться только активные
        assertEquals(2, active.size, "Должно быть 2 активных возможности")
        assertTrue(
            active.all { it.endTimestamp == null },
            "Все возвращенные возможности должны иметь endTimestamp == null"
        )
        assertTrue(
            active.any { it.id == id1 },
            "Должна присутствовать первая активная возможность"
        )
        assertTrue(
            active.any { it.id == id2 },
            "Должна присутствовать вторая активная возможность"
        )
    }

    @Test
    fun `test findActive returns empty list when all opportunities ended`() = runTest {
        // Given: Репозиторий только с завершенными возможностями
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC", endTimestamp = 1640995300))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH", endTimestamp = 1640995400))

        // When: Получаем активные
        val active = repository.findActive()

        // Then: Должен вернуться пустой список
        assertTrue(active.isEmpty(), "Не должно быть активных возможностей")
    }

    @Test
    fun `test markAsEnded sets endTimestamp`() = runTest {
        // Given: Репозиторий с активной возможностью
        val repository = InMemoryArbitrageOpportunityRepository()
        val id = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC", endTimestamp = null))

        // When: Помечаем как завершенную
        val endTime = Timestamp(1640995300)
        repository.markAsEnded(id, endTime)

        // Then: endTimestamp должен быть установлен
        val updated = repository.findById(id)
        assertNotNull(updated, "Возможность должна существовать")
        assertEquals(endTime, updated.endTimestamp, "endTimestamp должен быть установлен")
    }

    @Test
    fun `test markAsEnded removes opportunity from active list`() = runTest {
        // Given: Репозиторий с активной возможностью
        val repository = InMemoryArbitrageOpportunityRepository()
        val id = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC", endTimestamp = null))

        assertEquals(1, repository.findActive().size, "Предусловие: должна быть 1 активная")

        // When: Помечаем как завершенную
        repository.markAsEnded(id, Timestamp(1640995300))

        // Then: Не должна появляться в списке активных
        val active = repository.findActive()
        assertTrue(active.isEmpty(), "Возможность не должна быть в списке активных")
    }

    @Test
    fun `test markAsEnded does nothing for non-existent ID`() = runTest {
        // Given: Репозиторий с одной возможностью
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))

        // When: Пытаемся пометить несуществующую возможность
        // Then: Не должно быть исключения
        repository.markAsEnded(ArbitrageOpportunityId("non-existent"), Timestamp(1640995300))
    }

    @Test
    fun `test clear removes all opportunities`() = runTest {
        // Given: Репозиторий с несколькими возможностями
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH"))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT"))

        assertEquals(3, repository.findAll().size, "Предусловие: должно быть 3 возможности")

        // When: Очищаем репозиторий
        repository.clear()

        // Then: Репозиторий должен быть пустым
        assertTrue(repository.findAll().isEmpty(), "После clear репозиторий должен быть пустым")
        assertTrue(repository.findActive().isEmpty(), "Активных возможностей не должно быть")
    }

    @Test
    fun `test clear resets ID counter`() = runTest {
        // Given: Репозиторий с несколькими возможностями
        val repository = InMemoryArbitrageOpportunityRepository()
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC"))
        repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH"))
        repository.clear()

        // When: Сохраняем новую возможность после clear
        val newId = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT"))

        // Then: Счетчик ID должен быть сброшен
        assertEquals("arb-1", newId.value, "После clear счетчик ID должен начаться с 1")
    }

    @Test
    fun `test repository handles large number of opportunities`() = runTest {
        // Given: Репозиторий
        val repository = InMemoryArbitrageOpportunityRepository()

        // When: Сохраняем большое количество возможностей
        val opportunities = (1..100).map { i ->
            StubsDataFactory.createArbitrageOpportunity(
                token = "TOKEN$i",
                buyPrice = 100.0 + i,
                sellPrice = 105.0 + i
            )
        }
        val ids = repository.saveAll(opportunities)

        // Then: Все возможности должны быть сохранены
        assertEquals(100, ids.size, "Должно быть сохранено 100 возможностей")
        assertEquals(100, repository.findAll().size, "findAll должен вернуть 100 возможностей")
    }

    @Test
    fun `test save overwrites opportunity with same ID`() = runTest {
        // Given: Репозиторий с возможностью
        val repository = InMemoryArbitrageOpportunityRepository()
        val customId = "custom-123"
        val opp1 = StubsDataFactory.createArbitrageOpportunity(id = customId, token = "BTC", spread = 2.0)
        repository.save(opp1)

        // When: Сохраняем новую возможность с тем же ID
        val opp2 = StubsDataFactory.createArbitrageOpportunity(id = customId, token = "ETH", spread = 5.0)
        repository.save(opp2)

        // Then: Должна остаться только последняя
        val found = repository.findById(ArbitrageOpportunityId(customId))
        assertNotNull(found, "Возможность должна существовать")
        assertEquals("ETH", found.cexTokenId.value, "Токен должен обновиться")
        assertEquals(5.0, found.spread.value, "Spread должен обновиться")
        assertEquals(1, repository.findAll().size, "Должна быть только одна запись (перезапись)")
    }

    @Test
    fun `test active and ended opportunities coexist`() = runTest {
        // Given: Репозиторий с смешанными состояниями
        val repository = InMemoryArbitrageOpportunityRepository()

        val id1 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "BTC", endTimestamp = null))
        val id2 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "ETH", endTimestamp = 1640995300))
        val id3 = repository.save(StubsDataFactory.createArbitrageOpportunity(token = "USDT", endTimestamp = null))

        // When: Получаем все и активные
        val all = repository.findAll()
        val active = repository.findActive()

        // Then: Все возможности присутствуют, но только некоторые активны
        assertEquals(3, all.size, "Должно быть всего 3 возможности")
        assertEquals(2, active.size, "Должно быть 2 активных возможности")
    }
}
