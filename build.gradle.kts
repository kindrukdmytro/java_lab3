plugins {
    java
}

group = "ua.kpi.comsys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}