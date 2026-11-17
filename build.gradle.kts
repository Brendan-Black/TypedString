import java.util.zip.ZipFile

plugins {
    `java-library`
    `maven-publish`
    // Formatting and compiler warnings, checked by `check`: https://github.com/Brendan-Black/black-java
    id("black.java-standards") version "1.3.6"
}

// mavenCentral: the java-standards checks download their formatter and compiler from there.
repositories {
    mavenCentral()
}

group = "com.github.Brendan-Black"
version = providers.environmentVariable("VERSION").orElse("dev").get().removePrefix("v")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    main { java.setSrcDirs(listOf("src")) }
    test { java.setSrcDirs(listOf("test")) }
}

// Built with JDK 25, but runs on 17 and up: whatever implements it can target anything from 17.
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 17
    options.compilerArgs.add("-Xlint:all")
}

tasks.jar {
    manifest { attributes("Implementation-Version" to project.version) }
    from("LICENSE")
}

// The tests are a plain main() (no test framework).
val runTests = tasks.register<JavaExec>("runTests") {
    description = "Runs test/black/TypedStringTest."
    group = "verification"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "black.TypedStringTest"
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}
tasks.test { enabled = false }

// What users download must be exactly TypedString and LICENSE, with no dependencies in the POM or the module metadata.
val checkPackaging = tasks.register("checkPackaging") {
    description = "Fails if the published jar, POM or module metadata carries anything unexpected."
    group = "verification"
    val jar = tasks.jar
    val pom = tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication")
    val metadata = tasks.named<GenerateModuleMetadata>("generateMetadataFileForMavenPublication")
    dependsOn(jar, pom, metadata)
    doLast {
        val entries = ZipFile(jar.get().archiveFile.get().asFile).use { zip -> zip.entries().asSequence().map { it.name }.sorted().toList() }
        val expectedEntries = listOf("LICENSE", "META-INF/", "META-INF/MANIFEST.MF", "black/", "black/TypedString.class")
        check(entries == expectedEntries) { "the jar holds $entries, not $expectedEntries" }
        check("<dependency>" !in pom.get().destination.readText()) { "the POM lists a dependency" }
        check("\"dependencies\"" !in metadata.get().outputFile.get().asFile.readText()) { "the module metadata lists a dependency" }
    }
}
tasks.check { dependsOn(runTests, checkPackaging) }

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "TypedString"
            from(components["java"])
            pom {
                name = "TypedString"
                description = "black.TypedString: the marker for a type that holds and validates one shaped string."
                url = "https://github.com/Brendan-Black/TypedString"
                licenses { license { name = "MIT"; url = "https://opensource.org/licenses/MIT" } }
            }
        }
    }
}
