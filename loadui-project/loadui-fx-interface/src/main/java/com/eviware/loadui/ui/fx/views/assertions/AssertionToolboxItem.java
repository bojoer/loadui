package com.eviware.loadui.ui.fx.views.assertions;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;

public class AssertionToolboxItem extends Label
{
	protected static final Logger log = LoggerFactory.getLogger( CanvasView.class );

	private final AssertionItem<?> assertion;

	public AssertionToolboxItem( final AssertionItem<?> assertion )
	{
		this.assertion = assertion;

		getStyleClass().add( "icon" );

		setMaxHeight( 80 );
		setMinHeight( 80 );

		textProperty().bind( Properties.forLabel( assertion ) );

		final ImageView icon;
		Image image = UIUtils.getImageFor( assertion );
		if( image == null )
			log.debug( "No image found for holder " + assertion );
		icon = new ImageView( image );

		DragNode dragNode = DragNode.install( AssertionToolboxItem.this, new ImageView( icon.getImage() ) );
		dragNode.setData( assertion );

		setGraphic( icon );
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
