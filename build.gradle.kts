import com.google.protobuf.gradle.id

plugins {
    application

    jacoco
    id("net.razvan.jacoco-to-cobertura") version "1.2.0"

    id("com.google.protobuf") version "0.9.4"

    id("org.sonarqube") version "4.4.1.3373"
}

group = "ac.at.uibk.dps.cirrina"
version = "1.0-SNAPSHOT"

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
    implementation("com.beust:jcommander:1.82")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("com.google.protobuf:protobuf-java:3.25.3")

    implementation("io.nats:jnats:2.17.3")

    implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"));
    implementation("io.opentelemetry:opentelemetry-api");
    implementation("io.opentelemetry:opentelemetry-sdk");
    implementation("io.opentelemetry:opentelemetry-exporter-logging");
    implementation("io.opentelemetry:opentelemetry-exporter-otlp");
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.25.0-alpha");
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure");

    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    implementation("org.apache.commons:commons-jexl3:3.3")

    implementation("org.apache.curator:curator-framework:5.6.0")
    implementation("org.apache.curator:curator-recipes:5.6.0")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")

    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("org.glassfish.expressly:expressly:5.0.0")

    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.hibernate:hibernate-validator-cdi:8.0.1.Final")

    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")

    testImplementation("org.mockito:mockito-core:5.11.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

repositories {
    mavenCentral()
    maven(url = "https://repository.cloudera.com/artifactory/cloudera-repos/")
}

sonar {
    properties {
        property("sonar.projectKey", "cirrina-project_cirrina") // TODO: Change projectKey
        property("sonar.organization", "cirrina-project")       // TODO: Change organization
        property("sonar.host.url", "https://sonarcloud.io")
    }
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
    finalizedBy(tasks.jacocoToCobertura)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "at.ac.uibk.dps.cirrina.main.Main"
        attributes["Implementation-Version"] = version
    }
}
