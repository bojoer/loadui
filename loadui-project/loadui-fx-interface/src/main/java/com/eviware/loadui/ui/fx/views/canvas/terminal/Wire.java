package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveBuilder;

public class Wire extends Group
{
	private final CubicCurve outline = CubicCurveBuilder.create().fill( null ).stroke( Color.BLACK ).strokeWidth( 6 )
			.build();
	private final CubicCurve wire = CubicCurveBuilder.create().fill( null ).strokeWidth( 4 ).build();

	private boolean reverse = false;

	public Wire( double startX, double startY, double endX, double endY )
	{
		this();

		updatePosition( startX, startY, endX, endY );
	}

	public Wire()
	{
		getChildren().addAll( outline, wire );
	}

	public void setReversed( boolean reversed )
	{
		reverse = reversed;
	}

	public boolean isReversed()
	{
		return reverse;
	}

	public ObjectProperty<Paint> fillProperty()
	{
		return wire.strokeProperty();
	}

	public void setFill( Paint fill )
	{
		wire.setStroke( fill );
	}

	public Paint getFill()
	{
		return wire.getStroke();
	}

	public ObjectProperty<Paint> strokeProperty()
	{
		return outline.strokeProperty();
	}

	public void setStroke( Paint fill )
	{
		outline.setStroke( fill );
	}

	public Paint getStroke()
	{
		return outline.getStroke();
	}

	public void updatePosition( double startX, double startY, double endX, double endY )
	{
		if( reverse )
		{
			updatePoints( endX, endY, startX, startY );
		}
		else
		{
			updatePoints( startX, startY, endX, endY );
		}
	}

	private void updatePoints( double startX, double startY, double endX, double endY )
	{
		double control = Math.min( Math.sqrt( Math.pow( startX - endX, 2 ) + Math.pow( startY - endY, 2 ) ), 200 );

		outline.setStartX( startX );
		outline.setStartY( startY );
		outline.setControlX1( startX );
		outline.setControlY1( startY + control );
		outline.setControlX2( endX );
		outline.setControlY2( endY - control );
		outline.setEndX( endX );
		outline.setEndY( endY );

		wire.setStartX( startX );
		wire.setStartY( startY );
		wire.setControlX1( startX );
		wire.setControlY1( startY + control );
		wire.setControlX2( endX );
		wire.setControlY2( endY - control );
		wire.setEndX( endX );
		wire.setEndY( endY );
	}
}
