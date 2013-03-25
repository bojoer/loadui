package com.eviware.loadui.ui.fx.views.canvas.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.input.ContextMenuEvent;

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.LayoutContainerUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
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

		HasMenuItems hasMenuItems = MenuItemsProvider.createWith(
				this,
				getCanvasObject(),
				Options.are().settings( "Component Settings",
						LayoutContainerUtils.settingsTabsFromLayoutContainers( getComponent().getSettingsTabs() ) ) );
		menuButton.getItems().setAll( hasMenuItems.items() );
		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( hasMenuItems.items() ).build();

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );

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

	private void rebuildLayout()
	{
		Node layout = ComponentLayoutUtils.instantiateLayout( compactModeButton.isSelected() ? getComponent()
				.getCompactLayout() : getComponent().getLayout() );

		content.getChildren().setAll( layout );
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
