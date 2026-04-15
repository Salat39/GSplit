import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposePreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        dependencies {
            // Base
            implementation(findLib("androidx-activity-compose"))
            implementation(findLib("androidx-lifecycle-runtime-compose"))
            implementation(platform(findLibProvider("androidx-compose-bom")))
            implementation(findLib("androidx-ui"))
            implementation(findLib("androidx-ui-graphics"))
            implementation(findLib("androidx-ui-tooling-preview"))
            implementation(findLib("androidx-material3"))
            // implementation(findLib("androidx-compose-animation"))
            implementation(findLib("androidx-compose-foundation"))
            androidTestImplementation(platform(findLibProvider("androidx-compose-bom")))

            // Preview
            implementation(findLib("androidx-ui-tooling-preview"))
            debugImplementation(findLib("androidx-ui-tooling"))
        }
    }
}
