package com.arbitrage.scanner.libs.logging

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import ru.otus.otuskotlin.marketplace.logging.common.com.arbitrage.scanner.libs.logging.ArbScanLogWrapper
import kotlin.reflect.KClass

fun mpLoggerLogback(logger: Logger): ArbScanLogWrapper =
    ArbScanLogWrapperLogback(
        logger = logger,
        loggerId = logger.name,
    )

fun mpLoggerLogback(clazz: KClass<*>): ArbScanLogWrapper =
    mpLoggerLogback(LoggerFactory.getLogger(clazz.java) as Logger)

fun mpLoggerLogback(loggerId: String): ArbScanLogWrapper =
    mpLoggerLogback(LoggerFactory.getLogger(loggerId) as Logger)
