package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.RadioMenuItemBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import com.eviware.loadui.api.charting.line.ZoomLevel;

public class ZoomMenuButton extends MenuButton
{
	final private ToggleGroup toggleGroup = new ToggleGroup();

	private final ObjectProperty<ZoomLevel> selectedProperty = new ObjectPropertyBase<ZoomLevel>()
	{
		@Override
		public Object getBean()
		{
			return ZoomMenuButton.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

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

		toggleGroup.selectedToggleProperty().addListener( new ChangeListener<Toggle>()
		{

			@Override
			public void changed( ObservableValue<? extends Toggle> arg0, Toggle arg1, final Toggle newToggle )
			{
				Platform.runLater( new Runnable()
				{

					@Override
					public void run()
					{
						if( newToggle != null )
						{
							selectedProperty.setValue( ( ZoomLevel )newToggle.getUserData() );
						}

					}
				} );

			}

		} );

	}

	public ObjectProperty<ZoomLevel> selectedProperty()
	{
		return selectedProperty;
	}

	public ZoomLevel getSelected()
	{
		return selectedProperty.get();
	}

	public void setSelected( ZoomLevel selected )
	{
		for( Toggle t : toggleGroup.getToggles() )
		{
			if( ( ( ZoomLevel )t.getUserData() ).name().equals( selected.name() ) )
			{
				toggleGroup.selectToggle( t );
				break;
			}
		}

	}

}
