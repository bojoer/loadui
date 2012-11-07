package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsTab.Builder;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;

public class ComponentView extends CanvasObjectView
{
	private static final String COMPACT_MODE_ATTRIBUTE = "gui.compact";
	private final Observable layoutReloaded;
	private final ToggleButton compactModeButton;

	protected ComponentView( final ComponentItem component )
	{
		super( component );
		layoutReloaded = Properties.observeEvent( component, ComponentItem.LAYOUT_RELOADED );

		FXMLUtils.load( this, null, ComponentView.class.getResource( ComponentView.class.getSimpleName() + ".fxml" ) );

		menuButton.getItems().add(
				MenuItemBuilder.create().id( "settings" ).text( "Settings" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						settings();
					}
				} ).build() );

		compactModeButton = ToggleButtonBuilder.create().id( "compact" ).text( "C" )
				.selected( Boolean.parseBoolean( component.getAttribute( COMPACT_MODE_ATTRIBUTE, "false" ) ) )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						rebuildLayout();
						component.setAttribute( COMPACT_MODE_ATTRIBUTE, String.valueOf( compactModeButton.isSelected() ) );
					}
				} ).build();
		buttonBar.getChildren().add( 0, compactModeButton );

		layoutReloaded.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				rebuildLayout();
			}
		} );

		rebuildLayout();
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
			Builder tabBuilder = Builder.create( layoutContainer.getLabel() );
			layoutContainerToField( layoutContainer, tabBuilder );
			tabs.add( tabBuilder.build() );
		}
		SettingsDialog settingsDialog = new SettingsDialog( this, "Component Settings", tabs );
		settingsDialog.show();
	}

	private void rebuildLayout()
	{
		Node layout = null;
		boolean compact = compactModeButton.isSelected();
		if( compact )
		{
			layout = ComponentLayoutUtils.instantiateLayout( getComponent().getCompactLayout() );
		}
		else
		{
			layout = ComponentLayoutUtils.instantiateLayout( getComponent().getLayout() );
		}
		content.getChildren().setAll( layout );
	}

	private static void layoutContainerToField( LayoutContainer container, Builder tabBuilder )
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

	public static ComponentView newInstance( ComponentItem component )
	{
		if( component.getBehavior() instanceof OnOffCategory )
		{
			return new OnOffComponentView( component );
		}
		else
		{
			return new ComponentView( component );
		}
	}
}
