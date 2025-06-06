plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("checkstyle")
    id("application")
}

application {
    mainClass.set("com.cburch.logisim.Main")
}

group = "com.cburch.logisim"
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

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.cburch.logisim.Main"
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

checkstyle {
    toolVersion = "10.0"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
}