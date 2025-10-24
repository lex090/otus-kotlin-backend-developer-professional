plugins {
    alias(libs.plugins.build.plugin.jvm)
}

dependencies {

    api(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))

    implementation(libs.logback.classic)
    implementation(libs.logback.logstash)
    implementation(libs.logback.more.appenders)
    implementation(libs.fluent.logger)
}
