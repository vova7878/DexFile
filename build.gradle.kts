import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.maven.publish)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

dependencies {
    implementation(libs.android.misc)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = SourcesJar.Sources()
        )
    )

    coordinates(
        groupId = "io.github.vova7878",
        artifactId = "DexFile",
        version = project.version.toString()
    )

    pom {
        name.set("DexFile")
        description.set("Library for reading and writing dex files")
        inceptionYear.set("2025")
        url.set("https://github.com/vova7878/DexFile")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/license/mit")
                distribution.set("repository")
            }
        }

        developers {
            developer {
                id.set("vova7878")
                name.set("Vladimir Kozelkov")
                url.set("https://github.com/vova7878")
            }
        }

        scm {
            url.set("https://github.com/vova7878/DexFile")
            connection.set("scm:git:git://github.com/vova7878/DexFile.git")
            developerConnection.set("scm:git:ssh://git@github.com/vova7878/DexFile.git")
        }
    }
}
