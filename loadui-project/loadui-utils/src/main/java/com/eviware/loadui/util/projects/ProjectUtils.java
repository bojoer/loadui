package com.eviware.loadui.util.projects;

import java.util.NoSuchElementException;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;

public class ProjectUtils
{
	public static ProjectRef getProjectRef( ProjectItem project )
	{
		for( ProjectRef pRef : project.getWorkspace().getProjectRefs() )
		{
			if( pRef.getProjectId().equals( project.getId() ) )
			{
				return pRef;
			}
		}
		throw new NoSuchElementException( "Could not find a project reference for project " + project
				+ " in the current workspace." );
	}
}
