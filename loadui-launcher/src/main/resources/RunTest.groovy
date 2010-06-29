//Load the proper workspace
if( workspaceFile != null ) {
	workspace?.release()
	workspace = workspaceProvider.loadWorkspace( workspaceFile )
} else if( workspace == null ) {
	workspace = workspaceProvider.loadDefaultWorkspace()
}

//Get the project. Import it if needed.
def projectRef = null
for( ref in workspace.projectRefs ) {
	if( ref.projectFile.absolutePath == projectFile.absolutePath ) {
		projectRef = ref
		break
	}
}
if( projectRef == null ) projectRef = workspace.importProject( projectFile, true )
def project = projectRef.getProject()

println "Project: ${project}"

project.release()
workspace.release()