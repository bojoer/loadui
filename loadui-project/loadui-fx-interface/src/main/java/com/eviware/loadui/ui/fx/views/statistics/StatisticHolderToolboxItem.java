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
package com.eviware.loadui.ui.fx.views.statistics;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;

public class StatisticHolderToolboxItem extends Label
{
	protected static final Logger log = LoggerFactory.getLogger( StatisticHolderToolboxItem.class );

	private final StatisticHolder holder;

	public StatisticHolderToolboxItem( final StatisticHolder holder )
	{
		this.holder = holder;

		getStyleClass().add( "icon" );

		setMaxHeight( 80 );
		setMinHeight( 80 );

		textProperty().bind( Properties.forLabel( holder ) );

		final ImageView icon;
		Image image = UIUtils.getImageFor( holder );
		icon = new ImageView( image );

		DragNode dragNode = DragNode.install( StatisticHolderToolboxItem.this, new ImageView( icon.getImage() ) );
		dragNode.setData( holder );

		setGraphic( icon );
	}

	public StatisticHolder getHolder()
	{
		return holder;
	}

	@Override
	public String toString()
	{
		return holder.getLabel();
	}
}
