package com.eviware.loadui.ui.fx.views.canvas;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Objects;

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

		ImageView icon = new ImageView( Objects.firstNonNull( descriptor.getIcon(), "" ).toString() );
		DragNode dragNode = DragNode.install( this, new ImageView( icon.getImage() ) );
		dragNode.setData( descriptor );

		setGraphic( icon );
	}

	@Override
	public String toString()
	{
		return descriptor.getLabel();
	}
}
