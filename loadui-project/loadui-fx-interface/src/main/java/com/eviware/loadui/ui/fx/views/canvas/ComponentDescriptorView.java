/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.canvas;

import java.net.MalformedURLException;

import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;

public class ComponentDescriptorView extends Label
{
	private final ComponentDescriptor descriptor;
	private final VBox vbox;
	private Label label;

	public ComponentDescriptorView( ComponentDescriptor descriptor )
	{
		this.descriptor = descriptor;
		vbox = VBoxBuilder.create().spacing( 6 ).maxHeight( 68 ).minHeight( 68 ).build();
		getStyleClass().add( "icon" );
		
		try
		{
			Image image = new Image( descriptor.getIcon().toURL().toString(), 72, 0, true, true );
			
			ImageView icon = new ImageView( image );
			
			
			DragNode dragNode = DragNode.install( vbox, new ImageView( icon.getImage() ) );
			dragNode.setData( descriptor );
			
			
			vbox.getChildren().add( icon );
		}
		catch( MalformedURLException e )
		{
			e.printStackTrace();
		}

		textProperty().bind( Properties.forLabel( descriptor ) );
		label = LabelBuilder.create().id( "component" ).build();
		label.textProperty().bind( Properties.forLabel( descriptor ) );
   	label.setWrapText( true );
		label.maxWidth( 80 );

		vbox.getChildren().add(label);
		this.setGraphic( vbox );
	}

	public ComponentDescriptor getDescriptor()
	{
		return descriptor;
	}

	@Override
	public String toString()
	{
		return descriptor.getLabel();
	}
}
