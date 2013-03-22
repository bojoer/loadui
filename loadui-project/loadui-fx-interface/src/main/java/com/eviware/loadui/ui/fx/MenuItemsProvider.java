package com.eviware.loadui.ui.fx;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;

import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Labeled.Mutable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsTab;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;
import com.google.common.base.Preconditions;

/**
 * Any standard GUI component which has a menu should use this provider to get
 * its menu items.
 * <p/>
 * Supported menu items include Open, Close, Clone, Rename, Delete, Save, Create
 * and Settings.
 * 
 * @author renato
 * 
 */
public class MenuItemsProvider
{

	private static final ContextMenuEventHandler ctxMenuHandler = new ContextMenuEventHandler();

	public static HasMenuItems createWith( Node eventFirer, Object eventArg, Options options )
	{
		return new HasMenuItems( itemsFor( eventArg, eventFirer, options ) );
	}

	private static MenuItem[] itemsFor( Object eventArg, Node firer, Options options )
	{
		List<MenuItem> items = new ArrayList<>( 6 );
		if( options.open )
			items.add( itemFor( "open-item", options.openLabel,
					eventHandler( IntentEvent.INTENT_OPEN, firer, eventArg, options.openActions ) ) );
		if( options.rename && eventArg instanceof Mutable )
			items.add( itemFor( "rename-item", options.renameLabel,
					eventHandler( IntentEvent.INTENT_RENAME, firer, ( Mutable )eventArg ) ) );
		if( options.save )
			items.add( itemFor( "save-item", options.saveLabel, eventHandler( IntentEvent.INTENT_SAVE, firer, eventArg ) ) );
		if( options.clone )
			items.add( itemFor( "clone-item", options.cloneLabel, eventHandler( IntentEvent.INTENT_CLONE, firer, eventArg ) ) );
		if( options.delete && eventArg instanceof Deletable )
			items.add( itemFor( "delete-item", options.deleteData.deleteLabel,
					deleteHandler( firer, ( Deletable )eventArg, options.deleteData ) ) );
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
				SettingsDialog settingsDialog = new SettingsDialog( firer, data.dialogTitle, data.tabs );
				settingsDialog.show();
			}
		};
	}

	private static <T> EventHandler<ActionEvent> eventHandler( final EventType<IntentEvent<? extends T>> eventType,
			final Node firer, final T target, final Runnable... actions )
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				firer.fireEvent( IntentEvent.create( eventType, target ) );
				for( Runnable action : actions )
					Platform.runLater( action );
			}
		};
	}

	private static <T> EventHandler<ActionEvent> deleteHandler( final Node firer, final Deletable target,
			final Options.DeleteData data )
	{
		final EventHandler<ActionEvent> handler = eventHandler( IntentEvent.INTENT_DELETE, firer, target,
				data.deleteActions );
		return data.confirmDelete ? new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				String name = ( target instanceof Labeled ) ? "'" + ( ( Labeled )target ).getLabel() + "'" : "this item";
				ConfirmationDialog dialog = new ConfirmationDialog( firer, "Are you sure you want to delete " + name + "?",
						"Delete" );
				dialog.setOnConfirm( handler );
				dialog.show();
			}
		} : handler;
	}

	/**
	 * This should be used when you want to add a context menu to a component
	 * which does not support it. In JavaFX, only Control sub-classes support
	 * ContextMenu natively.
	 * 
	 * @param owner
	 *           any node in the DOM
	 * @param ctxMenu
	 *           to be shown
	 */
	public static void showContextMenu( Node owner, ContextMenu ctxMenu )
	{
		ctxMenuHandler.requestContextMenu( owner, ctxMenu );
	}

	private static class ContextMenuEventHandler implements EventHandler<MouseEvent>
	{

		final Rectangle wholeWindowRec = new Rectangle( 5000, 5000, Color.TRANSPARENT );
		MainWindowView mainWindowView;
		ContextMenu currentMenu;

		@Override
		public void handle( MouseEvent _ )
		{
			System.out.println( "Clicked on the wholeWindow Rectangle!!!!!" );
			mainWindowView.getChildren().remove( wholeWindowRec );
			currentMenu.hide();
			currentMenu.removeEventHandler( MouseEvent.MOUSE_CLICKED, this );
		}

		void requestContextMenu( Node owner, final ContextMenu ctxMenu )
		{
			if( mainWindowView == null )
			{
				mainWindowView = ( MainWindowView )owner.getScene().getRoot();
				wholeWindowRec.addEventHandler( MouseEvent.MOUSE_CLICKED, this );
			}

			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			currentMenu = ctxMenu;
			ctxMenu.show( owner, mouseLocation.getX(), mouseLocation.getY() );
			mainWindowView.getChildren().add( wholeWindowRec );
			ctxMenu.setOnHiding( new EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent _ )
				{
					ContextMenuEventHandler.this.handle( null );
					ctxMenu.setOnHiding( null );
				}
			} );
		}
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
		private final DeleteData deleteData = new DeleteData();
		private final SettingsData settingsData = new SettingsData();
		private String renameLabel = "Rename";
		private Runnable[] openActions = new Runnable[0];

		private class DeleteData
		{
			String deleteLabel = "Delete";
			Runnable[] deleteActions = new Runnable[0];
			boolean confirmDelete = true;
		}

		private class SettingsData
		{
			private String label = "Settings";
			private String dialogTitle;
			private List<? extends SettingsTab> tabs;
		}

		public static Options are()
		{
			return new Options();
		}

		public Options open( Runnable... actions )
		{
			open = true;
			openActions = actions;
			return this;
		}

		public Options open( String label, Runnable... actions )
		{
			openLabel = label;
			return open( actions );
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

		public Options settings( String dialogTitle, List<? extends SettingsTab> tabs )
		{
			settings = true;
			settingsData.dialogTitle = dialogTitle;
			settingsData.tabs = tabs;
			return this;
		}

		public Options settings( String dialogTitle, List<? extends SettingsTab> tabs, String label )
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

		public Options delete( Runnable... actions )
		{
			deleteData.deleteActions = actions;
			return this;
		}

		public Options delete( boolean confirmDelete, Runnable... actions )
		{
			deleteData.confirmDelete = confirmDelete;
			return delete( actions );
		}

		public Options delete( String label, boolean confirmDelete, Runnable... actions )
		{
			deleteData.deleteLabel = label;
			return delete( confirmDelete, actions );
		}

	}

}
