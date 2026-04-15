import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class FirebasePreset : Plugin<Project> {
    override fun apply(project: Project) = project.run {
        dependencies {
            implementation(platform(findLibProvider("firebase-bom")))
            implementation(findLib("firebase-analytics"))
            // implementation(findLib("firebase-perf"))
            // implementation(findLib("firebase-core"))
            implementation(findLib("firebase-crashlytics"))
            // implementation(findLib("firebase-messaging"))
            // implementation(findLib("firebase-config"))
        }
    }
}
