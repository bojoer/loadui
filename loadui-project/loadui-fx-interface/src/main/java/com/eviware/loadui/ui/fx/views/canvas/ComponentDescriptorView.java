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
