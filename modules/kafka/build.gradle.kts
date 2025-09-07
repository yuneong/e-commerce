plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api("org.springframework.kafka:spring-kafka")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")

    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.springframework.kafka:spring-kafka-test")
    testFixturesImplementation("org.testcontainers:kafka")
    testFixturesImplementation("org.springframework:spring-test")
}
