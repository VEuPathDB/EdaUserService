//
// Version Numbers
//
val coreLib   = "6.0.1"  // Container core lib version
val edaCommon = "8.1.0"  // EDA Common version

val jersey    = "3.0.4"   // Jersey/JaxRS version
val jackson   = "2.12.2" // FasterXML Jackson version
val junit     = "5.7.1"  // JUnit version
val log4j     = "2.16.0" // Log4J version
val metrics   = "0.9.0"  // Prometheus lib version

val implementation by configurations
val runtimeOnly    by configurations

val testImplementation by configurations
val testRuntimeOnly    by configurations

// use local EdaCommon compiled schema if project exists, else use released version;
//    this mirrors the way we use local EdaCommon code if available
val edaCommonLocalProjectDir = findProject(":edaCommon")?.projectDir
val edaCommonSchemaFetch =
  if (edaCommonLocalProjectDir != null)
    "cat ${edaCommonLocalProjectDir}/schema/library.raml"
  else
    "curl https://raw.githubusercontent.com/VEuPathDB/EdaCommon/v${edaCommon}/schema/library.raml"

// register a task that prints the command to fetch EdaCommon schema; used to pull down raml lib
tasks.register("print-eda-common-schema-fetch") { print(edaCommonSchemaFetch) }

dependencies {

  //
  // FgpUtil & Compatibility Dependencies
  //

  // FgpUtil jars
  implementation(files(
    "vendor/fgputil-accountdb-1.0.0.jar",
    "vendor/fgputil-core-1.0.0.jar",
    "vendor/fgputil-db-1.0.0.jar",
    "vendor/fgputil-web-1.0.0.jar",
    "vendor/fgputil-json-1.0.0.jar"
  ))

  // Compatibility bridge to support the long dead log4j-1.X
  runtimeOnly("org.apache.logging.log4j:log4j-1.2-api:${log4j}")

  // Extra FgpUtil dependencies
  runtimeOnly("org.apache.commons:commons-dbcp2:2.+")
  implementation("org.json:json:20190722")
  runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-json-org:${jackson}")
  runtimeOnly("com.fasterxml.jackson.module:jackson-module-parameter-names:${jackson}")
  runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${jackson}")
  runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jackson}")

  //
  // Project Dependencies
  //

  // Oracle
  runtimeOnly(files(
    "vendor/ojdbc8.jar",
    "vendor/ucp.jar",
    "vendor/xstreams.jar"
  ))

  // VEuPathDB libs, prefer local checkouts if available
  implementation(findProject(":core") ?: "org.veupathdb.lib:jaxrs-container-core:${coreLib}")
  implementation(findProject(":edaCommon") ?: "org.veupathdb.service.eda:eda-common:${edaCommon}")

  // Jersey
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:${jersey}")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${jersey}")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:${jersey}")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2:${jersey}")

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:${jackson}")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jackson}")

  // Stub database (temporary?)
  implementation("org.hsqldb:hsqldb:2.5.1")

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:${log4j}")
  implementation("org.apache.logging.log4j:log4j-core:${log4j}")
  implementation("org.apache.logging.log4j:log4j:${log4j}")

  // Metrics
  implementation("io.prometheus:simpleclient:${metrics}")
  implementation("io.prometheus:simpleclient_common:${metrics}")

  // Utils
  implementation("io.vulpine.lib:Jackfish:1.+")
  implementation("com.devskiller.friendly-id:friendly-id:1.+")

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter-api:${junit}")
  testImplementation("org.mockito:mockito-core:2.+")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit}")
}
