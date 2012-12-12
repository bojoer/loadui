package com.eviware.loadui.ui.fx.util;

import javafx.scene.image.Image;

import javax.annotation.Nullable;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.ImageResolver;
import com.eviware.loadui.util.BeanInjector;

public class CoreImageResolver implements ImageResolver
{
	public final static String TOOLBOX_IMAGES_PATH = "/com/eviware/loadui/ui/fx/toolboxIcons/";

	@Override
	@Nullable
	public Image resolveImageFor( Object object )
	{
		if( object instanceof AgentItem || AgentItem.class.equals( object ) )
		{
			return new Image( root( "agent-icon.png" ) );
		}
		else if( object instanceof ProjectItem || ProjectItem.class.equals( object ) )
		{
			return new Image( root( "project-icon.png" ) );
		}
		else if( object instanceof SceneItem || SceneItem.class.equals( object ) )
		{
			return new Image( root( "testcase-icon.png" ) );
		}
		else if( object instanceof ComponentItem )
		{
			return new Image( BeanInjector.getBean( ComponentRegistry.class )
					.findDescriptor( ( ( ComponentItem )object ).getType() ).getIcon().toString() );
		}
		else if( object instanceof AssertionItem )
		{
			return new Image( root( "assertion_icon_toolbar.png" ) );
		}
		else
		{
			return null;
		}
	}

	private static String root( String fileName )
	{
		return CoreImageResolver.class.getResource( TOOLBOX_IMAGES_PATH + fileName ).toExternalForm();
	}
}
