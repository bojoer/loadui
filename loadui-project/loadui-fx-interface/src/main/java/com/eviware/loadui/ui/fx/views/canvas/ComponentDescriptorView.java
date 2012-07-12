package com.eviware.loadui.ui.fx.views.canvas;

import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Objects;

public class ComponentDescriptorView extends VBox
{
	private final ComponentDescriptor descriptor;

	public ComponentDescriptorView( ComponentDescriptor descriptor )
	{
		this.descriptor = descriptor;
		setMaxHeight( 80 );
		setMinHeight( 80 );

		Label label = LabelBuilder.create().wrapText( true ).build();
		label.textProperty().bind( Properties.forLabel( descriptor ) );

		ImageView icon = new ImageView( Objects.firstNonNull( descriptor.getIcon(), "" ).toString() );
		DragNode dragNode = DragNode.install( this, new ImageView( icon.getImage() ) );
		dragNode.setData( descriptor );

		getChildren().setAll( icon, label );
	}
}
