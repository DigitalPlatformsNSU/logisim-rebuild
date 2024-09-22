plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

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
            "libs/MRJAdapter.jar",
        )
    )
}

tasks.test {
    useJUnitPlatform()
}