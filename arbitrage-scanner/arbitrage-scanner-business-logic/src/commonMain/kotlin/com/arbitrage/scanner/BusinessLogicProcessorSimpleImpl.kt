package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.handlers.chain
import com.crowdproj.kotlin.cor.handlers.worker
import com.crowdproj.kotlin.cor.rootChain

class BusinessLogicProcessorSimpleImpl(
    deps: BusinessLogicProcessorSimpleDeps
) : BusinessLogicProcessor {

    override suspend fun exec(ctx: Context) = corChain.exec(context = ctx)

    private val corChain = rootChain<Context, BusinessLogicProcessorSimpleDeps>(config = deps) {
        worker {
            title = "Начинаем обработку запроса."
            on { true }
            handle { state = State.RUNNING }
        }

        chain {
            title = "Обработка события read"
            on { command == Command.RECALCULATE && state == State.RUNNING }
            chain {
                title = "Обработка логики стабов"
                on { workMode == WorkMode.STUB && state == State.RUNNING }
                worker {
                    title = "Обработка стаба SUCCESS"
                    on { stubCase == Stubs.SUCCESS && state == State.RUNNING }
                    handle {

                    }
                }

                worker {
                    title = "Обработка стаба NOT_FOUND"
                    on { stubCase == Stubs.NOT_FOUND && state == State.RUNNING }
                    handle {

                    }
                }

                worker {
                    title = "Обработка стаба BAD_ID"
                    on { stubCase == Stubs.BAD_ID && state == State.RUNNING }
                    handle {

                    }
                }

                worker {
                    title = "Обработка стаба NONE"
                    on { stubCase == Stubs.NONE && state == State.RUNNING }
                    handle {

                    }
                }
            }

        }

        chain {
            title = "Обработка события search"
            on { command == Command.SEARCH && state == State.RUNNING }
            worker {

            }
        }

        chain {
            title = "Обработка события recalculate"
            on { command == Command.RECALCULATE && state == State.RUNNING }
        }
    }.build()
}