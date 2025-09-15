dependencies {
    implementation(project(":modules:jpa"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))
    implementation(project(":supports:monitoring"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    //batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    testImplementation(testFixtures(project(":modules:jpa")))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}
