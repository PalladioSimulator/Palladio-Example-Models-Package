import groovy.util.XmlSlurper
import org.apache.commons.io.FileUtils

/*
// set by the maven build
def examples_source = "initiatorTemplates"
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
projects = []
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
				
		projects.add([
			entityName: entityName,
			documentation: documentation,
			defaultInstanceURI: parent_folder.name
		])
	}
}

projects.sort { a, b -> a.entityName <=> b.entityName }

result = """<?xml version="1.0" encoding="UTF-8"?>
<org.palladiosimulator.architecturaltemplates:Catalog xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:org.palladiosimulator.architecturaltemplates="http://palladiosimulator.org/ArchitecturalTemplates/1.0" id="GeneratedATCatalog" entityName="Generated AT Catalog">\n"""

projects.each {
	result += """\t<ATs id="${it.entityName}.ID" entityName="${it.entityName}" documentation="${it.documentation}" defaultInstanceURI="${it.defaultInstanceURI}/"/>\n"""
}

result += """</org.palladiosimulator.architecturaltemplates:Catalog>\n"""


def catalogue = new File(catalogue_target)
catalogue.write result