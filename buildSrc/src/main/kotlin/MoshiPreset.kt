import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class MoshiPreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        apply {
            plugin(findPlugin("ksp"))
        }

        dependencies {
            implementation(findLib("moshi"))
            ksp(findLib("moshi-codegen"))
        }
    }
}
