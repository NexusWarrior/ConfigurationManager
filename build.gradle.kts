plugins {
    kotlin("jvm") version "2.4.10"
    application
}

group = "ru.soulways.configurationmanager"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass = "ru.soulways.configurationmanager.MainKt"
}

kotlin {
    jvmToolchain(25)
}

sourceSets {
    test {
        kotlin.srcDir("tests/kotlin")
    }
}

tasks.test {
    useJUnitPlatform()
}
