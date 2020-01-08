import groovy.util.XmlSlurper

def examples_source = "git@github.com:PalladioSimulator/Palladio-Example-Models.git"
def examples_target = "bundles/org.palladiosimulator.examples.package/initiatorTemplates"
def catalogue_target = "bundles/org.palladiosimulator.examples.package/Examples.architecturaltemplates"

println "Cloning projects from ${examples_source} to ${examples_target}"
gitClone = ["git", "clone", examples_source, examples_target].execute()
gitClone.waitFor()


projects = []
new File(examples_target).eachFileRecurse {
	if (it.name == ".project") {
		projects.add(new File(it.parent))
	}
}
projects = projects.sort()


result = """<?xml version="1.0" encoding="UTF-8"?>
<org.palladiosimulator.architecturaltemplates:Catalog xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:org.palladiosimulator.architecturaltemplates="http://palladiosimulator.org/ArchitecturalTemplates/1.0" id="GeneratedATCatalog" entityName="Generated AT Catalog">\n"""

projects.each {
	projectXml = new XmlSlurper().parse(new File("${it}/.project"))
	entityName = projectXml.name
	documentationFile = new File("${it}/.documentation")
	documentation = documentationFile.exists()
		? documentationFile.text
		: ""
		
	assert it.path.startsWith(examples_target + "/")
	defaultInstanceURI = it.path - (examples_target + "/")
	
	result += """\t<ATs id="${entityName}.ID" entityName="${entityName}" documentation="${documentation}" defaultInstanceURI="${defaultInstanceURI}/"/>\n"""
}

result += """</org.palladiosimulator.architecturaltemplates:Catalog>\n"""


def catalogue = new File(catalogue_target)
catalogue.write result