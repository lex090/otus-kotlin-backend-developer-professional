package com.arbitrage.scanner.libs.logging

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun arbScanLoggerLogback(logger: Logger): ArbScanLogWrapper =
    ArbScanLogWrapperLogback(
        logger = logger,
        loggerId = logger.name,
    )

fun arbScanLoggerLogback(clazz: KClass<*>): ArbScanLogWrapper =
    arbScanLoggerLogback(LoggerFactory.getLogger(clazz.java) as Logger)

fun arbScanLoggerLogback(loggerId: String): ArbScanLogWrapper =
    arbScanLoggerLogback(LoggerFactory.getLogger(loggerId) as Logger)
