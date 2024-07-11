plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.islandswars"
version = "0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.pkg.github.com/islands-wars/commons")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.mongodb:mongodb-driver-reactivestreams:5.0.0")
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    implementation("com.rabbitmq:amqp-client:5.21.0")
    implementation("org.apache.logging.log4j:log4j-core:3.0.0-beta2")
    implementation("com.github.docker-java:docker-java-core:3.3.6")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.3.6")
    implementation("it.unimi.dsi:fastutil:8.2.1")
    implementation("fr.islandswars:commons:0.4.4")
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fr.islandswars.ineundo.Ineundo"
    }
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = version

            artifact(sourceJar)
            artifact(javadocJar)

            pom {
                name.set(rootProject.name)
                description.set("Connect together islands wars servers")
                url.set("https://github.com/islands-wars/ineundo")

                licenses {
                    license {
                        name.set("The GNU General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html#license-text")
                    }
                }
                developers {
                    developer {
                        id.set("Xharos")
                        name.set("Burgaud Valentin")
                        email.set("jangliu@islandswars.fr")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/islands-wars/ineundo.git")
                    developerConnection.set("scm:git:ssh://github.com:islands-wars/ineundo.git")
                    url.set("https://github.com/islands-wars/ineundo")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/islands-wars/ineundo")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}