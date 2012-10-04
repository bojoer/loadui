package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.LinkedList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItemBuilder;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTabBuilder;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;

public class ComponentView extends CanvasObjectView
{
	public ComponentView( ComponentItem component )
	{
		super( component );
		//FXMLUtils.load( content, null, ComponentView.class.getResource( ComponentView.class.getSimpleName() + ".fxml" ) );

		menuButton.getItems().add(
				MenuItemBuilder.create().id( "settings" ).text( "Settings" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						settings();
					}
				} ).build() );

		LayoutComponent layoutComponent = component.getLayout();
		content.getChildren().add( ComponentLayoutUtils.instantiateLayout( layoutComponent ) );
	}

	public ComponentItem getComponent()
	{
		return ( ComponentItem )getCanvasObject();
	}

	public void settings()
	{
		List<SettingsTab> tabs = new LinkedList<>();
		for( SettingsLayoutContainer layoutContainer : getComponent().getSettingsTabs() )
		{
			SettingsTabBuilder tabBuilder = SettingsTabBuilder.create( layoutContainer.getLabel() );
			layoutContainerToField( layoutContainer, tabBuilder );
			tabs.add( tabBuilder.build() );
		}
		SettingsDialog settingsDialog = new SettingsDialog( this, "Component Settings", tabs );
		settingsDialog.show();
	}

	private static void layoutContainerToField( LayoutContainer container, SettingsTabBuilder tabBuilder )
	{
		for( LayoutComponent component : container )
		{
			log.debug( "LayoutComponent class: " + component.getClass().getCanonicalName() );
			if( component instanceof PropertyLayoutComponent )
			{
				PropertyLayoutComponent<?> propertyComponent = ( PropertyLayoutComponent<?> )component;
				tabBuilder.field( propertyComponent.getLabel(), propertyComponent.getProperty() );
			}
			else if( component instanceof LayoutContainer )
			{
				layoutContainerToField( ( LayoutContainer )component, tabBuilder );
			}
		}
	}
}
