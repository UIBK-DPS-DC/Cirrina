plugins {
    application

    jacoco
    id("net.razvan.jacoco-to-cobertura") version "1.2.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.11"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")

    testImplementation("org.mockito:mockito-core:5.11.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = false
        csv.required = false
    }
}
tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    finalizedBy(tasks.jacocoToCobertura)
}