import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RetrofitPreset : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        dependencies {
            implementation(findLib("retrofit"))
            implementation(findLib("retrofit-moshi"))
            implementation(findLib("okhttp-interceptor"))
            testImplementation(findLib("okhttp-mockserver"))
        }
    }
}
