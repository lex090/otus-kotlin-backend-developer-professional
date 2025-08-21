# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a multi-module Kotlin project for OTUS Kotlin Backend Developer Professional course. The project uses Gradle composite builds with three main components:

- `lessons/` - Educational modules with course lessons (e.g., `m1l1-first`)
- `main-project/` - Main application modules including multiplatform modules
- `build-logic/` - Custom Gradle build plugins for standardized configuration
- `arbitrage-scanner/` - Application module (appears to be empty/placeholder)

## Build System

The project uses Gradle with Kotlin DSL and custom build plugins located in `build-logic/`:

- `BuildPluginJvm` - Configures JVM-only Kotlin modules 
- `BuildPluginMultiplatform` - Configures Kotlin Multiplatform modules with JVM, Linux, and macOS targets

Version catalog is defined in `gradle/libs.versions.toml` with Kotlin 2.1.21 and Java 21.

## Common Development Commands

### Building
```bash
./gradlew build                    # Build all modules
./gradlew :lessons:build          # Build lessons modules
./gradlew :main-project:build     # Build main project modules
```

### Testing
```bash
./gradlew test                    # Run all tests
./gradlew :lessons:m1l1-first:test    # Run specific lesson tests
```

The test framework used is JUnit Platform with Kotlin Test.

### Running Applications
```bash
./gradlew :lessons:m1l1-first:run     # Run lesson application
./gradlew :main-project:tmp-module:run # Run main project application
```

## Architecture Notes

- Uses composite builds (`includeBuild`) to separate concerns between lessons, main project, and build logic
- Custom build plugins standardize Java 21 toolchain configuration across modules
- Multiplatform modules support JVM, Linux x64, macOS ARM64, and macOS x64 targets
- Progressive Kotlin language features are enabled in multiplatform modules
- All modules share common group `com.education.project` and version `1.0-SNAPSHOT`

## Module Types

- **JVM modules**: Use `alias(libs.plugins.build.plugin.jvm)` plugin
- **Multiplatform modules**: Use `alias(libs.plugins.build.plugin.multiplatform)` plugin
- **Lesson modules**: Located in `lessons/` with simple structure and tests
- **Application modules**: Support both commonMain and platform-specific source sets