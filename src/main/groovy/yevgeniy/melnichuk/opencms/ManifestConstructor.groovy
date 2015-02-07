package yevgeniy.melnichuk.opencms

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil


class ManifestConstructor implements Writable {
    List<String> snippets
    String template

    def appendFilesFromXML(GPathResult manifest, GPathResult snippet) {
        manifest.files.appendNode(snippet.files.file)
    }

    def appendResourceTypesFromXML(GPathResult manifest, GPathResult snippet) {
        manifest.module.resourcetypes.appendNode(snippet.module.resourcetypes.type)
    }

    @Override
    public Writer writeTo(Writer out) throws IOException {
        GPathResult manifest = new XmlSlurper().parseText(template)

        snippets.each {
            GPathResult snippet = new XmlSlurper().parseText(it)

            appendFilesFromXML(manifest, snippet)
            appendResourceTypesFromXML(manifest, snippet)
        }

        out << XmlUtil.serialize(manifest)
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter()
        writeTo(writer);

        return writer
    }
}
