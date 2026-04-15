import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class CoilPreset : Plugin<Project> {
    override fun apply(project: Project) = project.run {
        dependencies {
            implementation(findLib("coil-compose"))
            implementation(project(Modules.CORE_COIL))
        }
    }
}
