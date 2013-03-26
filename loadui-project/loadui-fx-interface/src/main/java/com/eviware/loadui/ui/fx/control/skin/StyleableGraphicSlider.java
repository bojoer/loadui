package com.eviware.loadui.ui.fx.control.skin;

import static java.lang.Math.round;

import java.awt.Point;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.sun.javafx.scene.control.skin.SliderSkin;

public class StyleableGraphicSlider extends SliderSkin
{

	private StackPane thumb;
	private final Tooltip tooltip;

	public StyleableGraphicSlider( final Slider slider )
	{
		super( slider );
		tooltip = new Tooltip( getTooltipText( slider ) );

		slider.valueProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				showTooltip( slider );
			}
		} );

		Platform.runLater( new Runnable()
		{

			boolean mouseIsPressed;

			public void run()
			{
				thumb = ( StackPane )slider.lookup( ".thumb" );

				thumb.addEventHandler( MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>()
				{

					@Override
					public void handle( MouseEvent _ )
					{
						showTooltip( slider );
					}

				} );
				thumb.addEventHandler( MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
				{

					@Override
					public void handle( MouseEvent _ )
					{
						if( !mouseIsPressed )
						{
							tooltip.hide();
						}

					}
				} );
				thumb.addEventHandler( MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
				{
					@Override
					public void handle( MouseEvent _ )
					{
						mouseIsPressed = true;
					}
				} );
				thumb.addEventHandler( MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
				{
					@Override
					public void handle( MouseEvent _ )
					{
						mouseIsPressed = false;
						if( !NodeUtils.isMouseOn( thumb ) )
						{
							System.out.println( "Hiding the tooltip" );
							Platform.runLater( new Runnable()
							{

								@Override
								public void run()
								{
									tooltip.hide(); // TODO Auto-generated method stub

								}
							} );
						}
					}
				} );
				thumb.getChildren().add(
						RegionBuilder.create().styleClass( "graphic" ).minHeight( 6 ).minWidth( 12 ).build() );
			}
		} );
	}

	private void showTooltip( final Slider slider )
	{
		tooltip.setText( getTooltipText( slider ) );
		Point ml = NodeUtils.getAbsMouseLocation();
		tooltip.show( slider.getScene().getWindow(), ml.getX(), ml.getY() );
	}

	private static String getTooltipText( final Slider slider )
	{
		int intVal = ( int )round( slider.getValue() );
		String res = "";
		if( intVal < 0 )
		{
			for( int i = 0; i < -1 - intVal; i++ )
				res += "0";
			res = "0." + res + "1";
		}
		else if( intVal == 0 )
		{
			res = "1.0";
		}
		else
		{
			res = "1";
			for( int i = 0; i < intVal; i++ )
				res += "0";
		}
		return res;
	}

}
