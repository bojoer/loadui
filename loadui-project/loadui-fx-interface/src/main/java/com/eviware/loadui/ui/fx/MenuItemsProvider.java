package com.eviware.loadui.ui.fx;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;

import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled.Mutable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.google.common.base.Preconditions;

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

	public static class Options
	{

		private boolean open = false;
		private boolean close = false;
		private boolean create = false;
		private boolean clone = false;
		private boolean save = false;
		private boolean rename = true;
		private boolean delete = true;
		private Class<?> typeToCreate;
		private String openLabel = "Open";
		private String closeLabel = "Close";
		private String createLabel = "Create";
		private String cloneLabel = "Clone";
		private String saveLabel = "Save";
		private String renameLabel = "Rename";
		private String deleteLabel = "Delete";

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
