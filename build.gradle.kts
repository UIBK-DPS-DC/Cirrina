plugins {
    id("java")
}

group = "ac.at.uibk.dps.cirrina"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("dev.cel:cel:0.3.1")
    implementation("org.apache.commons:commons-jexl3:3.3")

    implementation("io.nats:jnats:2.17.3")

    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.hibernate:hibernate-validator-cdi:8.0.1.Final")

    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")

    implementation("org.glassfish.expressly:expressly:5.0.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.glassfish:jakarta.el:4.0.2")

    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks {
    javadoc {
        options {
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
            overview = "src/main/javadoc/overview.html" // Relative to source root
            addBooleanOption("-allow-script-in-comments", true)
            header = "<script type=\"text/javascript\" async src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML\"></script>"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
