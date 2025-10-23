package com.arbitrage.scanner

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.handlers.chain
import com.crowdproj.kotlin.cor.handlers.worker
import com.crowdproj.kotlin.cor.rootChain

class BusinessLogicProcessorStubsImpl(
    deps: BusinessLogicProcessorSimpleDeps,
) : BusinessLogicProcessor {

    override suspend fun exec(ctx: Context) = corChain.exec(context = ctx)

    private val corChain = rootChain<Context, BusinessLogicProcessorSimpleDeps>(config = deps) {
        worker {
            title = "Начинаем обработку запроса."
            on { true }
            handle { state = State.RUNNING }
        }

        chain {
            title = "Обработка события recalculate"
            on { command == Command.RECALCULATE && state == State.RUNNING }
            chain {
                title = "Обработка логики стабов"
                on { workMode == WorkMode.STUB && state == State.RUNNING }

                worker {
                    title = "Обработка стаба SUCCESS"
                    on { stubCase == Stubs.SUCCESS && state == State.RUNNING }
                    handle {
                        recalculateResponse = ArbOpStubs.recalculateResult
                        state = State.FINISHING
                    }
                }
            }

            worker {
                title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах"
                on { state == State.RUNNING }
                handle {
                    fail(
                        InternalError(
                            code = "validation",
                            group = "validation",
                            field = "stub",
                            message = "Wrong stub case is requested: ${stubCase.name}. Command: ${command.name}"
                        )
                    )
                }
            }
        }

        chain {
            title = "Обработка события read"
            on { command == Command.READ && state == State.RUNNING }
            chain {
                title = "Обработка логики стабов"
                on { workMode == WorkMode.STUB && state == State.RUNNING }
                worker {
                    title = "Обработка стаба SUCCESS"
                    on { stubCase == Stubs.SUCCESS && state == State.RUNNING }
                    handle {
                        arbitrageOpportunityReadResponse = ArbOpStubs.arbitrageOpportunity
                        state = State.FINISHING
                    }
                }

                worker {
                    title = "Обработка стаба NOT_FOUND"
                    on { stubCase == Stubs.NOT_FOUND && state == State.RUNNING }
                    handle {
                        fail(
                            InternalError(
                                code = "not-found",
                                group = "stub",
                                field = "id",
                                message = "Арбитражная возможность не найдена"
                            )
                        )
                    }
                }

                worker {
                    title = "Обработка стаба BAD_ID"
                    on { stubCase == Stubs.BAD_ID && state == State.RUNNING }
                    handle {
                        fail(
                            InternalError(
                                code = "bad-id",
                                group = "stub",
                                field = "id",
                                message = "Некорректный идентификатор арбитражной возможности"
                            )
                        )
                    }
                }
            }

            worker {
                title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах"
                on { state == State.RUNNING }
                handle {
                    fail(
                        InternalError(
                            code = "validation",
                            group = "validation",
                            field = "stub",
                            message = "Wrong stub case is requested: ${stubCase.name}. Command: ${command.name}"
                        )
                    )
                }
            }
        }

        chain {
            title = "Обработка события search"
            on { command == Command.SEARCH && state == State.RUNNING }
            chain {
                title = "Обработка логики стабов"
                on { workMode == WorkMode.STUB && state == State.RUNNING }

                worker {
                    title = "Обработка стаба SUCCESS"
                    on { stubCase == Stubs.SUCCESS && state == State.RUNNING }
                    handle {
                        arbitrageOpportunitySearchResponse.add(ArbOpStubs.arbitrageOpportunity)
                        state = State.FINISHING
                    }
                }

                worker {
                    title = "Обработка стаба NOT_FOUND"
                    on { stubCase == Stubs.NOT_FOUND && state == State.RUNNING }
                    handle {
                        arbitrageOpportunitySearchResponse.clear()
                        state = State.FINISHING
                    }
                }
            }

            worker {
                title = "Валидируем ситуацию, когда запрошен кейс, который не поддерживается в стабах"
                on { state == State.RUNNING }
                handle {
                    fail(
                        InternalError(
                            code = "validation",
                            group = "validation",
                            field = "stub",
                            message = "Wrong stub case is requested: ${stubCase.name}. Command: ${command.name}"
                        )
                    )
                }
            }
        }
    }.build()
}