import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

open class ShuttleAndroidExtension @Inject constructor(private val project: Project) {

    fun androidApp(
        id: String,
        versionCode: Int,
        versionName: String
    ) {
        project.extensions.configure<TestedExtension> {
            defaultConfig {
                this.applicationId = id
                this.versionCode = versionCode
                this.versionName = versionName
            }
        }
    }

    @Suppress("UnstableApiUsage")
    fun useCompose() {
        project.extensions.configure<TestedExtension> {
            buildFeatures.compose = true
            composeOptions.kotlinCompilerExtensionVersion = "1.2.0-dev-k1.7.0-53370d83bb1"
        }
    }

    companion object {

        fun setup(project: Project): ShuttleAndroidExtension =
            project.extensions.create("shuttleAndroid")
    }
}
