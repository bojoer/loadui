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
package com.eviware.loadui.ui.fx.views.assertions;

import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;

public class AssertionToolboxItem extends Label
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionToolboxItem.class );

	private final AssertionItem<?> assertion;
	private final VBox vbox; 
	
	public AssertionToolboxItem( final AssertionItem<?> assertion )
	{
		this.assertion = assertion;
		final Label label = LabelBuilder.create().id( "component" ).build();
		vbox = VBoxBuilder.create().spacing( 6 ).maxHeight( 68 ).minHeight( 68 ).children( createIcon( assertion ), label ).build();
		
		getStyleClass().add( "icon" );

		textProperty().bind( Properties.forLabel( assertion ) );
		
		label.textProperty().bind( Properties.forLabel( assertion ) );

		DragNode dragNode = DragNode.install( vbox, createIcon( assertion ) );
		dragNode.setData( assertion );

		setGraphic( vbox );
	}

	private ImageView createIcon( final AssertionItem<?> assertion){
		Image image = UIUtils.getImageFor( assertion );
		ImageView icon = new ImageView( image );
		icon.setPreserveRatio( true );
		icon.setFitHeight( 54 );
		return icon; 
	}
	
	public AssertionItem<?> getHolder()
	{
		return assertion;
	}

	@Override
	public String toString()
	{
		return assertion.getLabel();
	}
}
