import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getByType

/**
 * Dependencies shortcuts
 */
fun DependencyHandler.implementation(dependency: Any) {
    add("implementation", dependency)
}

fun DependencyHandler.test(dependency: Any) {
    add("test", dependency)
}

fun DependencyHandler.androidTest(dependency: Any) {
    add("androidTest", dependency)
}

fun DependencyHandler.debugImplementation(dependency: Any) {
    add("debugImplementation", dependency)
}

fun DependencyHandler.testImplementation(dependency: Any) {
    add("testImplementation", dependency)
}

fun DependencyHandler.androidTestImplementation(dependency: Any) {
    add("androidTestImplementation", dependency)
}

fun DependencyHandler.kapt(dependency: Any) {
    add("kapt", dependency)
}

fun DependencyHandler.ksp(dependency: Any) {
    add("ksp", dependency)
}

fun DependencyHandler.api(dependency: Any) {
    add("api", dependency)
}

fun DependencyHandler.annotationProcessor(dependency: Any) {
    add("annotationProcessor", dependency)
}

/**
 * Project version catalog scope shortcut
 */
private fun Project.libs(): VersionCatalog {
    return extensions.getByType<VersionCatalogsExtension>().named("libs")
}

/**
 * Find lib from version catalog by alias
 */
fun Project.findLib(alias: String): Any {
    return libs().findLibrary(alias).get()
}

/**
 * Find lib provider from version catalog by alias
 */
fun Project.findLibProvider(alias: String): Any {
    return libs().findLibrary(alias).get().get()
}

/**
 * Find plugin from version catalog by alias
 */
fun Project.findPlugin(alias: String): String {
    return libs().findPlugin(alias).get().get().pluginId
}

/**
 * Project android scope shortcut
 */
fun Project.android(): LibraryExtension {
    return extensions.getByType(LibraryExtension::class.java)
}

/**
 * Init from gradle scope
 */
// fun DependencyHandler.room() {
//    implementation("")
//    implementation("")
//    implementation("")
// }
