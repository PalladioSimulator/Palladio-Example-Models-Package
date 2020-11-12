import groovy.util.XmlSlurper
import org.apache.commons.io.FileUtils

/*
// set by the maven build
def examples_source = "target/examples"
def examples_target = "initiatorTemplates"
def catalogue_target = "Examples.architecturaltemplates"
*/

def deleteIfExists(String path) {
	file = new File(path)
	if (file.exists()) {
		println " -- Removing ${file.isDirectory() ? "directory" : "file"} ${file.name} (${file.path})"
		FileUtils.forceDelete(file)
	} else {
		println " -- ${path} not found, not removing"
	}
}

// create target directory if it does not exist
def examples_target_folder = new File(examples_target)
if (!examples_target_folder.exists()) {
	examples_target_folder.mkdirs()
}

// discover all folders that contain a .project file
println "Starting example project discovery:"

catalog_to_projects = [:]

examples_sources.split("\\s+").each { catalog_and_examples_source ->
	(example_catalog, examples_source) = catalog_and_examples_source.split("::")

	catalog_to_projects.putIfAbsent(example_catalog, [])

    println "Source: ${examples_source}"
    new File(examples_source).eachFileRecurse {
    	if (it.name == ".project") {
    		parent_folder = new File(it.parent)
    		
    		println " - ${parent_folder.name}"
    		target_folder = "${examples_target}/${parent_folder.name}"
    		
    		println " -- Copying ${parent_folder.path} into ${examples_target_folder.path}"
    		FileUtils.copyDirectoryToDirectory(parent_folder, examples_target_folder)
    		
    		projectFile = new File("${target_folder}/.project")
    		projectXml = new XmlSlurper().parse(projectFile)
    		entityName = "${projectXml.name}"
    		println " -- Discovered project name: ${entityName}"
    		
    		documentationFile = new File("${target_folder}/.documentation")
    		if (documentationFile.exists()) {
    			documentation = documentationFile.text
    			println " -- Documentation found (${documentationFile.path})"
    		} else {
    			documentation = ""
    			println " -- No documentation found"
    		}
    		
    		deleteIfExists(documentationFile.path)
    		deleteIfExists("${target_folder}/.settings")
    		deleteIfExists("${target_folder}/.classpath")
    		deleteIfExists(projectFile.path)
    				
    		catalog_to_projects[example_catalog].add([
    			entityName: entityName,
    			documentation: documentation,
    			defaultInstanceURI: parent_folder.name
    		])
    	}
    }
}

pluginXmlEntries = []

// build all architectural-template catalogs
catalog_to_projects.each{entry ->
	catalog = entry.key
	projects = entry.value

	// could be changed to replace "-" with " " or to allow better
	// naming of catalogs
	catalogName = catalog

	projects.sort { a, b -> a.entityName <=> b.entityName }

	result = """<?xml version="1.0" encoding="UTF-8"?>
	<org.palladiosimulator.architecturaltemplates:Catalog
		xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:org.palladiosimulator.architecturaltemplates="http://palladiosimulator.org/ArchitecturalTemplates/1.0"
		id="${catalog}.ID"
		entityName="${catalog}">\n"""

	projects.each {
		result += """\t<ATs id="${it.entityName}.ID" entityName="${it.entityName}" documentation="${it.documentation}" defaultInstanceURI="${it.defaultInstanceURI}/"/>\n"""
	}

	result += "</org.palladiosimulator.architecturaltemplates:Catalog>\n"


	catalogFileName = "${catalog}.architecturaltemplates"
	def catalogFile = new File("${catalog_target}/${catalogFileName}")
	catalogFile.write result

	pluginXmlEntries.add("platform:/plugin/org.palladiosimulator.examples.package/${catalogFileName}#${catalog}.ID")
}


// build plugin.xml

pluginXmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<?eclipse version="3.4"?>
<plugin>
"""
pluginXmlEntries.each {
	pluginXmlContent += """\t<extension point="org.palladiosimulator.architecturaltemplates.catalogs">\n\t\t<ATCatalog catalogURI="${it}"></ATCatalog>\n\t</extension>\n"""
}
pluginXmlContent += "\n</plugin>\n"


pluginXmlFile = new File("${catalog_target}/plugin.xml")
pluginXmlFile.write pluginXmlContent