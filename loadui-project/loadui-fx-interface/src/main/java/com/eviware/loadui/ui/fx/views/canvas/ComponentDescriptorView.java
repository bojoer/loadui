package com.eviware.loadui.ui.fx.views.canvas;

import java.net.MalformedURLException;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;

public class ComponentDescriptorView extends Label
{
	private final ComponentDescriptor descriptor;

	public ComponentDescriptorView( ComponentDescriptor descriptor )
	{
		this.descriptor = descriptor;

		getStyleClass().add( "icon" );

		setMaxHeight( 80 );
		setMinHeight( 80 );

		textProperty().bind( Properties.forLabel( descriptor ) );

		ImageView icon;
		try
		{
			icon = new ImageView( descriptor.getIcon().toURL().toString() );
			DragNode dragNode = DragNode.install( this, new ImageView( icon.getImage() ) );
			dragNode.setData( descriptor );

			setGraphic( icon );
		}
		catch( MalformedURLException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString()
	{
		return descriptor.getLabel();
	}
}
