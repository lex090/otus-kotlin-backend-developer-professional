package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.workers.commandProcessor
import com.arbitrage.scanner.workers.initStatus
import com.arbitrage.scanner.workers.stubs.noStubCaseWorker
import com.arbitrage.scanner.workers.stubs.readBadIdStubWorker
import com.arbitrage.scanner.workers.stubs.readNotFoundStubWorker
import com.arbitrage.scanner.workers.stubs.readSuccessStubWorker
import com.arbitrage.scanner.workers.stubs.recalculateSuccessStubWorker
import com.arbitrage.scanner.workers.stubs.searchNotFoundStubWorker
import com.arbitrage.scanner.workers.stubs.searchSuccessStubWorker
import com.arbitrage.scanner.workers.workModProcessor
import com.crowdproj.kotlin.cor.rootChain

class BusinessLogicProcessorImpl(
    deps: BusinessLogicProcessorImplDeps,
) : BusinessLogicProcessor {

    override suspend fun exec(ctx: Context) = corChain.exec(context = ctx)

    private val corChain = rootChain(config = deps) {
        initStatus("Инициализация статуса обработки запроса")

        commandProcessor(title = "Обработка события recalculate", command = Command.RECALCULATE) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                recalculateSuccessStubWorker(title = "Обработка стаба SUCCESS")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }
        }

        commandProcessor(title = "Обработка события read", command = Command.READ) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                readSuccessStubWorker(title = "Обработка стаба SUCCESS")
                readNotFoundStubWorker(title = "Обработка стаба NOT_FOUND")
                readBadIdStubWorker(title = "Обработка стаба BAD_ID")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }
        }

        commandProcessor(title = "Обработка события search", command = Command.SEARCH) {
            workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
                searchSuccessStubWorker(title = "Обработка стаба SUCCESS")
                searchNotFoundStubWorker(title = "Обработка стаба NOT_FOUND")
                noStubCaseWorker(title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах")
            }
        }
    }.build()
}