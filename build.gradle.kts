import org.veupathdb.lib.gradle.container.util.Logger.Level
import java.io.FileOutputStream
import java.net.URL

plugins {
  java
  id("org.veupathdb.lib.gradle.container.container-utils") version "4.6.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

// configure VEupathDB container plugin
containerBuild {

  // Change if debugging the build process is necessary.
  logLevel = Level.Info

  // General project level configuration.
  project {

    // Project Name
    name = "eda-user-service"

    // Project Group
    group = "org.veupathdb.service.eda"

    // Project Version
    version = "3.0.0"

    // Project Root Package
    projectPackage = "org.veupathdb.service.eda.us"

    // Main Class Name
    mainClassName = "Main"
  }

  // Docker build configuration.
  docker {

    // Docker build context
    context = "."

    // Name of the target docker file
    dockerFile = "Dockerfile"

    // Resulting image tag
    imageName = "eda-user"

  }

  generateJaxRS {
    // List of custom arguments to use in the jax-rs code generation command
    // execution.
    arguments = listOf(/*arg1, arg2, arg3*/)

    // Map of custom environment variables to set for the jax-rs code generation
    // command execution.
    environment = mapOf(/*Pair("env-key", "env-val"), Pair("env-key", "env-val")*/)
  }

}

tasks.register("print-gen-package") { print("org.veupathdb.service.eda") }

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks.shadowJar {
  exclude("**/Log4j2Plugins.dat")
  archiveFileName.set("service.jar")
}

repositories {
  mavenCentral()
  mavenLocal()
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = if (extra.has("gpr.user")) extra["gpr.user"] as String? else System.getenv("GITHUB_USERNAME")
      password = if (extra.has("gpr.key")) extra["gpr.key"] as String? else System.getenv("GITHUB_TOKEN")
    }
  }
}

//
// Project Dependencies
//

// versions
val coreLib       = "6.12.1"         // Container core lib version
val edaCommon     = "9.1.0"          // EDA Common version
val fgputil       = "2.7.1-jakarta"  // FgpUtil version

val jersey        = "3.0.4"       // Jersey/JaxRS version
val jackson       = "2.13.3"      // FasterXML Jackson version
val junit         = "5.8.2"       // JUnit version
val log4j         = "2.17.2"      // Log4J version
val metrics       = "0.15.0"      // Prometheus lib version


// use local EdaCommon compiled schema if project exists, else use released version;
//    this mirrors the way we use local EdaCommon code if available
val edaCommonLocalProjectDir = findProject(":edaCommon")?.projectDir

val commonRamlOutFileName = "$projectDir/schema/eda-common-lib.raml"

val mergeRamlTask = tasks.named("merge-raml");

val fetchEdaCommonRamlTask = tasks.register("fetch-eda-common-schema") {
  doLast {
    val commonRamlOutFile = File(commonRamlOutFileName)
    commonRamlOutFile.delete()

    if (edaCommonLocalProjectDir != null) {
      val commonRamlFile = File("${edaCommonLocalProjectDir}/schema/library.raml")
      logger.lifecycle("Copying file from ${commonRamlFile.path} to ${commonRamlOutFile.path}")
      commonRamlFile.copyTo(commonRamlOutFile);
    } else {
      commonRamlOutFile.createNewFile();
      val edaCommonRamlUrl = "https://raw.githubusercontent.com/VEuPathDB/EdaCommon/v${edaCommon}/schema/library.raml"
      logger.lifecycle("Downloading file contents from $edaCommonRamlUrl")
      URL(edaCommonRamlUrl).openStream().use { it.transferTo(FileOutputStream(commonRamlOutFile)) }
    }
  }
}
mergeRamlTask.get().dependsOn(fetchEdaCommonRamlTask)

val cleanEdaCommonSchemaTask = tasks.register("clean-eda-common-schema") {
  doLast {
    logger.lifecycle("Deleting file $commonRamlOutFileName")
    File(commonRamlOutFileName).delete()
  }
}
mergeRamlTask.get().finalizedBy(cleanEdaCommonSchemaTask)

// ensures changing modules are never cached
configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

dependencies {

  // VEuPathDB libs, prefer local checkouts if available
  implementation(findProject(":core") ?: "org.veupathdb.lib:jaxrs-container-core:${coreLib}")
  implementation(findProject(":edaCommon") ?: "org.veupathdb.service.eda:eda-common:${edaCommon}")

  // published VEuPathDB libs
  implementation("org.gusdb:fgputil-core:${fgputil}")
  implementation("org.gusdb:fgputil-accountdb:${fgputil}")
  implementation("org.gusdb:fgputil-db:${fgputil}")
  implementation("org.gusdb:fgputil-json:${fgputil}")

  // Jersey
  implementation("org.glassfish.jersey.core:jersey-server:${jersey}")

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:${jackson}")
  implementation("com.fasterxml.jackson.core:jackson-annotations:${jackson}")

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:${log4j}")
  implementation("org.apache.logging.log4j:log4j-core:${log4j}")

  // Metrics
  implementation("io.prometheus:simpleclient:${metrics}")
  implementation("io.prometheus:simpleclient_common:${metrics}")

  // Utils
  implementation("io.vulpine.lib:Jackfish:1.1.0")
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter-api:${junit}")
  testImplementation("org.mockito:mockito-core:4.6.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit}")

  // Stub database (temporary?)
  implementation("org.hsqldb:hsqldb:2.6.1")
}

val test by tasks.getting(Test::class) {
  // Use junit platform for unit tests
  useJUnitPlatform()
}
