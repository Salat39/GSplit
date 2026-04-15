import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        apply {
            plugin(findPlugin("ksp"))
            plugin(findPlugin("hilt"))
        }

        dependencies {
            implementation(findLib("hilt-android"))
            ksp(findLib("hilt-android-compiler"))
            implementation(findLib("androidx-hilt-compose"))
        }
    }
}
