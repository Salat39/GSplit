import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RoomPreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        apply {
            plugin(findPlugin("ksp"))
        }

        dependencies {
            api(findLib("androidx-room-runtime"))
            // annotationProcessor(findLib("androidx-room-compiler"))
            ksp(findLib("androidx-room-compiler"))
            implementation(findLib("androidx-room-ktx"))
        }
    }
}
