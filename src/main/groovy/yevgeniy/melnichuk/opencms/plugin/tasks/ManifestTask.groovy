package yevgeniy.melnichuk.opencms.plugin.tasks

import groovy.text.GStringTemplateEngine

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import yevgeniy.melnichuk.opencms.ManifestConstructor


class ManifestTask extends DefaultTask {

    @OutputFile
    def output

    @InputDirectory
    def input

    @TaskAction
    def run() {
        String template = resolvePlaceholders(ManifestTask.getResource('default-manifest-template.xml').text, [
            moduleName        : project.name,
            moduleAuthor      : project.opencms.author,
            moduleNicename    : project.name,
            moduleVersion     : project.version,
            moduleAuthorEmail : project.opencms.email
        ])

        output.withWriter {
            new ManifestConstructor(
            	template: template, 
            	snippets: collectSnippets()
			).writeTo(it);
        }
    }

    List<String> collectSnippets() {
        List<String> snippets = [];

        // we assume right order here (i.e. folder before files from the folder)
        input.eachFileRecurse { File file ->
            if (ignoreFile(file)) {
                return;
            }

            String manifestSnippet = getManifestSnippetForFile(file);
            String preparedManifestSnippet = resolvePlaceholders(manifestSnippet, [
                resource : getRelativePath(input, file),
                type     : getTypeForFile(file),
                
                // every time a new UUID might be a problem
                uuid     : UUID.randomUUID()
            ])

            snippets.add(preparedManifestSnippet);
        }

        return snippets;
    }

    boolean ignoreFile(File file) {
        return file.name.endsWith('.manifest.xml')
    }

    String getTypeForFile(File file) {
        if (file.directory) {
            return 'folder';
        }

        switch (FilenameUtils.getExtension(file.name)) {
            case 'jsp':
                return 'jsp'

            case 'png' :
            case 'bmp' :
            case 'jpg' :
            case 'jpeg' :
                return 'image';

            case 'dat' :
            case 'tgz' :
            case 'zip' :
            case 'rar' :
            case 'jar' :
                return 'binary'

            default:
                return 'plain';
        }

    }

    String getRelativePath(File aParentFolder, File file) {
        String relativePath = aParentFolder.toURI().relativize(file.toURI()).path
        if (relativePath.endsWith('/')) {
            relativePath = relativePath.substring(0, relativePath.length() - 1)
        }

        return "system/modules/${project.name}/${relativePath}"
    }

    String getManifestSnippetForFile(File file) {
        String manifestSnippetPath = "${file.absolutePath}.manifest.xml";
        File manifestSnippetFile = new File(manifestSnippetPath);
        if (manifestSnippetFile.exists()) {
            return manifestSnippetFile.text;
        }

        return loadDefaultSnippetForFile(file);
    }

    String loadDefaultSnippetForFile(File file) {
        if (file.directory) {
            return ManifestTask.getResource('default-manifest-folder-snippet.xml').text
        } else {
            return ManifestTask.getResource('default-manifest-file-snippet.xml').text
        }
    }

    String resolvePlaceholders(String template, Map properties) {
        return new GStringTemplateEngine().createTemplate(template).make(properties).toString()
    }
}