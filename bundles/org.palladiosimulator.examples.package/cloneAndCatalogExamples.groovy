import groovy.util.XmlSlurper

/*
// set by the maven build
def examples_source = "initiatorTemplates"
def examples_target = "initiatorTemplates"
def catalogue_target = "Examples.architecturaltemplates"
*/

// create target directory if it does not exist
def examples_target_filehandle = new File(examples_target)
if (!examples_target_filehandle.exists()) {
	examples_target_filehandle.mkdirs()
}


// discover all folders that contain a .project file
println "Starting example project discovery:"
projects = []
new File(examples_source).eachFileRecurse {
	if (it.name == ".project") {
		parent_folder = new File(it.parent)
		
		println " - ${parent_folder.name}"
		target_folder = "${examples_target}/${parent_folder.name}"
		
		command = ["cp", "-r", parent_folder.path, target_folder]
		println " -- Copying: " + command.join(" ")
		command.execute().waitFor()
		
		projectXml = new XmlSlurper().parse(new File("${target_folder}/.project"))
		entityName = "${projectXml.name}"
		println " -- Discovered project name: ${entityName}"
		
		documentationFile = new File("${target_folder}/.documentation")
		if (documentationFile.exists()) {
			documentation = documentationFile.text
			println " -- Documentation found"
		} else {
			documentation = ""
			println " -- No documentation found"
		}
		
		settingsFolder = new File("${target_folder}/.settings")
		if (settingsFolder.exists()) {
			command = ["rm" ,"-r", settingsFolder.path]
			println " -- Removing .settings: " + command.join(" ")
			command.execute().waitFor()
		}
	
		command = ["rm", "${target_folder}/.project"]
		println " -- Removing .project file: " + command.join(" ")
		command.execute().waitFor()
		
		command = ["rm", "${target_folder}/.documentation"]
		println " -- Removing .documentation file: " + command.join(" ")
		command.execute().waitFor()
		
		projects.add([
			entityName: entityName,
			documentation: documentation,
			defaultInstanceURI: entityName
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