import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ComposeLibConfig : Plugin<Project> {

    override fun apply(project: Project) {
        setProjectConfig(project)
    }

    private fun setProjectConfig(project: Project) {
        project.android().apply {
            compileSdk = ProjectConfig.COMPILE_SDK

            defaultConfig {
                minSdk = ProjectConfig.MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }
            buildTypes {
                release {
                    isMinifyEnabled = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
                maybeCreate("internal").apply {
                    initWith(getByName("release"))
                }
                maybeCreate("car").apply {
                    initWith(getByName("release"))
                }
            }
            buildFeatures {
                compose = true
            }
            compileOptions {
                sourceCompatibility = ProjectConfig.COMPATIBILITY_VERSION
                targetCompatibility = ProjectConfig.COMPATIBILITY_VERSION
            }
        }
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions {
                jvmTarget.set(ProjectConfig.JVM_TARGET)
            }
        }
    }
}
