import com.google.protobuf.gradle.id

plugins {
    application

    jacoco

    id("com.google.protobuf") version "0.9.4"
    id("org.pkl-lang") version "0.26.2"
}

group = "ac.at.uibk.dps.cirrina"
version = "1.0.0"

application {
    mainClass = "at.ac.uibk.dps.cirrina.main.Main"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.11"
}

pkl {
    javaCodeGenerators {
        register("pklGenJava") {
            allowedModules.add("https:")
            sourceModules.addAll(
                "https://raw.githubusercontent.com/UIBK-DPS-DC/Cirrina-Specifications/main/pkl/CollaborativeStateMachineDescription.pkl",
                "https://raw.githubusercontent.com/UIBK-DPS-DC/Cirrina-Specifications/main/pkl/HttpServiceImplementationDescription.pkl",
                "https://raw.githubusercontent.com/UIBK-DPS-DC/Cirrina-Specifications/main/pkl/JobDescription.pkl",
                "https://raw.githubusercontent.com/UIBK-DPS-DC/Cirrina-Specifications/main/pkl/ServiceImplementationDescription.pkl"
            )
            generateGetters.set(true)
            generateJavadoc.set(true)
        }
    }
}

protobuf {
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("python")
                id("cpp")
            }
        }
    }
}

dependencies {
    implementation("org.pkl-lang:pkl-config-java:0.26.2")
    implementation("org.pkl-lang:pkl-codegen-java:0.26.2")

    implementation("com.beust:jcommander:1.82")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("com.google.protobuf:protobuf-java:3.25.3")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.slf4j:slf4j-api:1.7.30")

    implementation("org.aspectj:aspectjrt:1.9.19")
    implementation("org.aspectj:aspectjweaver:1.9.19")

    implementation("io.nats:jnats:2.17.3")

    implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-exporter-logging")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:1.34.1")
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.25.0-alpha")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")

    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    implementation("org.apache.commons:commons-jexl3:3.3")

    implementation("org.apache.curator:curator-framework:5.6.0")
    implementation("org.apache.curator:curator-recipes:5.6.0")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")

    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("com.github.tkowalcz.tjahzi:core:tjahzi-0.9")
    implementation("pl.tkowalcz.tjahzi:log4j2-appender-nodep:0.9.17")

    implementation("org.glassfish.expressly:expressly:5.0.0")

    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.hibernate:hibernate-validator-cdi:8.0.1.Final")

    implementation("org.jgrapht:jgrapht-core:1.5.2")

    testImplementation("org.mockito:mockito-core:5.11.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = false
        csv.required = false
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "at.ac.uibk.dps.cirrina.main.Main"
        attributes["Implementation-Version"] = version
    }
}
