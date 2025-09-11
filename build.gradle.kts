import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
  id("java")

  id("org.jetbrains.intellij") version "1.17.3"

  // Gradle Changelog Plugin
  id("org.jetbrains.changelog") version "2.0.0"

  // Gradle Qodana Plugin
  id("org.jetbrains.qodana") version "0.1.13"

}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
  maven { url = uri("https://maven.aliyun.com/repository/public/") }
  maven { url = uri("https://maven.aliyun.com/repository/spring/") }
  maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
  maven { url = uri("https://maven.aliyun.com/repository/central/") }
  maven { url = uri("https://maven.aliyun.com/repository/spring-plugin/") }
  mavenCentral()
}

dependencies {
  implementation("com.fifesoft:rsyntaxtextarea:3.1.6")
  implementation("com.alibaba:easyexcel:4.0.2")
  implementation("org.slf4j:slf4j-api:2.0.13")
  implementation("org.slf4j:slf4j-simple:2.0.13")

  annotationProcessor("org.projectlombok:lombok:1.18.28");
  compileOnly("org.projectlombok:lombok:1.18.28")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.28");
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  plugins.set(properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) })

}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

//  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "17"
//  }

  wrapper {
    gradleVersion = properties("gradleVersion").get()
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(providers.fileContents(layout.projectDirectory.file("PLUGIN-DESC.md")).asText.map {
      val start = "<!-- Plugin description -->"
      val end = "<!-- Plugin description end -->"

      with (it.lines()) {
        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
      }
    })

    val changelog = project.changelog // local variable for configuration cache compatibility
    // Get the latest available change notes from the changelog file
    changeNotes.set(properties("pluginVersion").map { pluginVersion ->
      with(changelog) {
        renderItem(
          (getOrNull(pluginVersion) ?: getUnreleased())
            .withHeader(false)
            .withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      }
    })
  }

  // Configure UI tests plugin
  // Read more: https://github.com/JetBrains/intellij-ui-test-robot
  runIdeForUiTests {
    systemProperty("robot-server.port", "8082")
    systemProperty("ide.mac.message.dialogs.as.sheets", "false")
    systemProperty("jb.privacy.policy.text", "<!--999.999-->")
    systemProperty("jb.consents.confirmation.enabled", "false")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
