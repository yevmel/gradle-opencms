package yevgeniy.melnichuk.opencms.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class OpenCmsPluginConfiguration {

    @Input
    @Optional
    String home

    @Input
    String author

    @Input
    String email

    public OpenCmsPluginConfiguration(Project project) {
        author = System.properties['user.name'] ?: ""
        email = "${author}@localhost"
    }
}