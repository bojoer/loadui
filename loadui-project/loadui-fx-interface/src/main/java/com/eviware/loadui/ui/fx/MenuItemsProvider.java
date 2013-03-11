package com.eviware.loadui.ui.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;

import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled.Mutable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.util.LayoutContainerUtils;
import com.google.common.base.Preconditions;

/**
 * Any standard GUI component which has a menu should use this provider to get its menu items.
 * <p/>
 * Supported menu items include Open, Close, Clone, Rename, Delete, Save, Create and Settings. 
 * @author renato
 *
 */
public class MenuItemsProvider
{

	public static HasMenuItems createWith( Node eventFirer, Object eventArg, Options options )
	{
		return new HasMenuItems( itemsFor( eventArg, eventFirer, options ) );
	}

	private static MenuItem[] itemsFor( Object eventArg, Node firer, Options options )
	{
		List<MenuItem> items = new ArrayList<>( 6 );
		if( options.open )
			items.add( itemFor( "open-item", options.openLabel, eventHandler( IntentEvent.INTENT_OPEN, firer, eventArg ) ) );
		if( options.rename && eventArg instanceof Mutable )
			items.add( itemFor( "rename-item", options.renameLabel,
					eventHandler( IntentEvent.INTENT_RENAME, firer, ( Mutable )eventArg ) ) );
		if( options.save )
			items.add( itemFor( "save-item", options.saveLabel, eventHandler( IntentEvent.INTENT_SAVE, firer, eventArg ) ) );
		if( options.clone )
			items.add( itemFor( "clone-item", options.cloneLabel, eventHandler( IntentEvent.INTENT_CLONE, firer, eventArg ) ) );
		if( options.delete && eventArg instanceof Deletable )
			items.add( itemFor( "delete-item", options.deleteLabel,
					eventHandler( IntentEvent.INTENT_DELETE, firer, ( Deletable )eventArg ) ) );
		if( options.close )
			items.add( itemFor( "close-item", options.closeLabel, eventHandler( IntentEvent.INTENT_CLOSE, firer, eventArg ) ) );
		if( options.settings )
			items.add( itemFor( "settings-item", options.settingsData, firer ) );
		if( options.create )
		{
			if( items.size() > 2 )
				items.add( SeparatorMenuItemBuilder.create().build() );
			items.add( itemFor( "create-item", options.createLabel,
					eventHandler( IntentEvent.INTENT_CREATE, firer, options.typeToCreate ) ) );
		}
		return items.toArray( new MenuItem[items.size()] );
	}

	public static class HasMenuItems
	{

		private MenuItem[] items;

		private HasMenuItems( MenuItem[] items )
		{
			this.items = items;
		}

		public MenuItem[] items()
		{
			return items;
		}

	}

	private static MenuItem itemFor( String id, String label, EventHandler<ActionEvent> handler )
	{
		return MenuItemBuilder.create().text( label ).id( id ).onAction( handler ).build();
	}

	private static MenuItem itemFor( String id, Options.SettingsData data, Node firer )
	{
		return MenuItemBuilder.create().text( data.label ).id( id ).onAction( settingsHandlerFor( data, firer ) ).build();
	}

	private static EventHandler<ActionEvent> settingsHandlerFor( final Options.SettingsData data, final Node firer )
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				SettingsDialog settingsDialog = new SettingsDialog( firer, data.dialogTitle,
						LayoutContainerUtils.settingsTabsFromLayoutContainers( data.tabs ) );
				settingsDialog.show();
			}
		};
	}

	private static <T> EventHandler<ActionEvent> eventHandler( final EventType<IntentEvent<? extends T>> eventType,
			final Node firer, final T target )
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				firer.fireEvent( IntentEvent.create( eventType, target ) );
			}
		};
	}

	/**
	 * Options for construction of menu items.
	 * <p/>
	 * All options are false by default, except:
	 * <ul>
	 * <li>rename: if the event argument is an instance of Labeled.Mutable, the
	 * menu item will contain the rename item unless noRename() is called.</li>
	 * <li>delete: if the event argument is an instance of Deletable, the menu
	 * item will contain the delete item unless noDelete() is called.</li>
	 * </ul>
	 * 
	 * @author renato
	 * 
	 */
	public static class Options
	{

		private boolean open = false;
		private boolean close = false;
		private boolean create = false;
		private boolean clone = false;
		private boolean save = false;
		private boolean settings = false;
		private boolean rename = true;
		private boolean delete = true;
		private Class<?> typeToCreate;
		private String openLabel = "Open";
		private String closeLabel = "Close";
		private String createLabel = "Create";
		private String cloneLabel = "Clone";
		private String saveLabel = "Save";
		private final SettingsData settingsData = new SettingsData();
		private String renameLabel = "Rename";
		private String deleteLabel = "Delete";

		private class SettingsData
		{
			private String label = "Settings";
			private String dialogTitle;
			private Collection<? extends SettingsLayoutContainer> tabs;
		}

		public static Options are()
		{
			return new Options();
		}

		public Options open()
		{
			open = true;
			return this;
		}

		public Options open( String label )
		{
			openLabel = label;
			return open();
		}

		public Options close()
		{
			close = true;
			return this;
		}

		public Options close( String label )
		{
			closeLabel = label;
			return close();
		}

		public Options create( Class<?> type )
		{
			create = true;
			typeToCreate = Preconditions.checkNotNull( type );
			return this;
		}

		public Options create( Class<?> type, String label )
		{
			createLabel = label;
			return create( type );
		}

		public Options clone()
		{
			clone = true;
			return this;
		}

		public Options clone( String label )
		{
			cloneLabel = label;
			return clone();
		}

		public Options save()
		{
			save = true;
			return this;
		}

		public Options save( String label )
		{
			saveLabel = label;
			return save();
		}

		public Options settings( String dialogTitle, Collection<? extends SettingsLayoutContainer> tabs )
		{
			settings = true;
			settingsData.dialogTitle = dialogTitle;
			settingsData.tabs = tabs;
			return this;
		}

		public Options settings( String dialogTitle, Collection<? extends SettingsLayoutContainer> tabs, String label )
		{
			settingsData.label = label;
			return settings( dialogTitle, tabs );
		}

		public Options noRename()
		{
			rename = false;
			return this;
		}

		public Options rename( String label )
		{
			renameLabel = label;
			return this;
		}

		public Options noDelete()
		{
			delete = false;
			return this;
		}

		public Options delete( String label )
		{
			deleteLabel = label;
			return this;
		}

	}

}
