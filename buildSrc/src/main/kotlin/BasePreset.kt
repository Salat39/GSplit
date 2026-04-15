import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class BasePreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        dependencies {
            // Logger
            implementation(findLib("timber"))

            // Leakcanary
            debugImplementation(findLib("leakcanary-watcher"))
            debugImplementation(findLib("leakcanary"))
        }
    }
}
