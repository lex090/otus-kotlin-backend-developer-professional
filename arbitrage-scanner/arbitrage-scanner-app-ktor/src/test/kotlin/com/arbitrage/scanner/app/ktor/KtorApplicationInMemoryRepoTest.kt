package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.app.ktor.koin.modules.blModuleTest
import org.koin.core.module.Module

class KtorApplicationInMemoryRepoTest : KtorApplicationBaseTest() {
    override val workMode: ArbitrageOpportunityRequestDebugModeApi = ArbitrageOpportunityRequestDebugModeApi.TEST

    override val readModule: Module = blModuleTest(
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )

    override val searchModule: Module = blModuleTest(
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )

    override val recalculateModule: Module = blModuleTest(
        initServiceObject = initServiceObject,
        initRepoObject = initRepoObject,
    )
}
