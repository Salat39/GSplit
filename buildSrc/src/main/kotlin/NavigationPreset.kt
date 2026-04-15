import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class NavigationPreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
//        apply {
//            plugin(findPlugin("kotlinSerialization"))
//        }

        dependencies {
            implementation(findLib("androidx-navigation-compose"))
            implementation(findLib("kotlinx-serialization-json"))
        }
    }
}
