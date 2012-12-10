package com.eviware.loadui.ui.fx.views.analysis;

import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.RadioMenuItemBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import com.eviware.loadui.api.charting.line.ZoomLevel;

public class ZoomMenuButton extends MenuButton
{
	final private ToggleGroup toggleGroup = new ToggleGroup();

	public ZoomMenuButton()
	{
		super( null, null );

		ZoomLevel[] values = ZoomLevel.values();
		for( int i = values.length - 1; i >= 0; i-- )
		{
			ZoomLevel z = values[i];
			RadioMenuItem added = RadioMenuItemBuilder.create().text( z.name() ).userData( z ).build();
			added.setToggleGroup( toggleGroup );
			this.getItems().add( added );
		}

	}

	public ToggleGroup getToggleGroup()
	{
		return toggleGroup;
	}

	public void setSelected( ZoomLevel z )
	{
		for( Toggle t : getToggleGroup().getToggles() )
		{
			if( ( ( ZoomLevel )t.getUserData() ).name().equals( z.name() ) )
			{
				getToggleGroup().selectToggle( t );
				break;
			}
		}
	}

}
