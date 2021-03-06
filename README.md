# Palladio-Example-Models-Package
This project repackages Palladio Example models as initiator templates to make them available in the "New Palladio Project - Sirius" wizard.

## Documentation
The projects are scraped from [the Palladio-Example-Models repository](https://github.com/PalladioSimulator/Palladio-Example-Models).
All folders that contain a .project file are discovered and copied (including nested folders).
The following files/folders are removed from the copied folders if they exist to avoid messing with the newly created Eclipse projects:
* .project
* .classpath
* .settings
* .description

The *name* of the entry in the wizard is equal to the name of the Eclipse project (*not* the name of the folder).
The *description* of the entry is filled with the content of the .description file, if it exists.

## Support
For support
* visit our [issue tracking system](https://palladio-simulator.com/jira)
* contact us via our [mailing list](https://lists.ira.uni-karlsruhe.de/mailman/listinfo/palladio-dev)

For professional support, please fill in our [contact form](http://www.palladio-simulator.com/about_palladio/support/).
