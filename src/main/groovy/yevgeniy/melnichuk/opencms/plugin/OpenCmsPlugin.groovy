package yevgeniy.melnichuk.opencms.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import yevgeniy.melnichuk.opencms.plugin.tasks.ManifestTask

class OpenCmsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        OpenCmsPluginConfiguration pluginConfiguration = project.extensions.create("opencms", OpenCmsPluginConfiguration, project);

        project.dependencies.ext.localOpenCms = {
            if (pluginConfiguration.home == null) {
                throw new InvalidUserDataException('missing "home" property.')
            }

            project.fileTree(dir: "${pluginConfiguration.home}/WEB-INF/lib/", include: ['*.jar'])
        }

        project.task('copyResources', type: DefaultTask) {
            group "OpenCMS"

            inputs.file project.tasks.jar
            inputs.file "src/main/opencms"

            outputs.file "${project.buildDir}/opencms/"

            doLast {
                project.copy {
                    from "src/main/opencms"
                    into "${project.buildDir}/opencms/"
                }

                project.copy {
                    from project.tasks.jar
                    into "${project.buildDir}/opencms/lib/"
                }
            }
        }

        project.task('generateManifest', type: ManifestTask) {
            description 'generates manifest.xml.'
            group "OpenCMS"

            inputs.file project.tasks.copyResources

            output = project.file("${project.buildDir}/manifest.xml");
            input = project.file("${project.buildDir}/opencms");
        }

        project.task('packageModule', type: Zip, dependsOn: "generateManifest") {
            description 'creates a OpenCMS module.'
            group "OpenCMS"

            exclude '**/*.manifest.xml'

            from("${project.buildDir}/opencms") { into "system/modules/${project.name}" }
            from(project.tasks.generateManifest)
        }
    }
}
