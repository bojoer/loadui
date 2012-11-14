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
	protected static final Logger log = LoggerFactory.getLogger( CanvasView.class );

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
		if( image == null )
			log.debug( "No image found for holder " + holder );
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
