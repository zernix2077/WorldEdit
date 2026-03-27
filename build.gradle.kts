plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
}

group = "xyz.zernix.worldedit"
description = "Allay-native WorldEdit implementation"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// See also https://github.com/AllayMC/AllayGradle
allay {
    api = "0.27.0"
    server = "0.11.0"
    apiOnly = false

    plugin {
        entrance = ".WorldEditPlugin"
        authors += "Zernix"
        website = "https://github.com/Zernix/WorldEdit"
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}
