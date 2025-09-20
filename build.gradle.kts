plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.maven.publish)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withSourcesJar()
}

dependencies {
    implementation(libs.android.misc)
}

publishing {
    publications {
        create<MavenPublication>("javaMaven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}