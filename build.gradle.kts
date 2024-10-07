plugins {
    id("java")
    id("checkstyle")
}

group = "org.example"
version = "1.0-SNAPSHOT"

checkstyle {
    toolVersion = "10.3.3"
}

tasks.withType(Checkstyle) {
    reports {
        xml.required = false
        html.required = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(
        files(
            "libs/colorpicker.jar",
            "libs/fontchooser.jar",
            "libs/jh.jar",
            "libs/MRJAdapter.jar"
        )
    )
}

tasks.test {
    useJUnitPlatform()
}
