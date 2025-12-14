plugins {
    java
    `java-library`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    
    jar {
        archiveBaseName.set("Rpstrana")
        archiveVersion.set("1.0.0")
    }
}