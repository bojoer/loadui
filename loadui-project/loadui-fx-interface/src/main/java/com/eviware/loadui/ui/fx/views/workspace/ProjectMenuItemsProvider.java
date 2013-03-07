package com.eviware.loadui.ui.fx.views.workspace;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;

public class ProjectMenuItemsProvider
{

	public static SimpleMenuItemHolder createWith( Node eventFirer )
	{
		return new SimpleMenuItemHolder( eventFirer );
	}

	public static ProjectMenuItemHolder createWith( Node eventFirer, ProjectRef ref )
	{
		return new ProjectMenuItemHolder( eventFirer, ref );
	}


	public static class ProjectMenuItemHolder extends SimpleMenuItemHolder
	{

		private final ProjectRef ref;

		public ProjectMenuItemHolder( Node eventFirer, ProjectRef ref )
		{
			super( eventFirer );
			this.ref = ref;
		}

		@Override
		public MenuItem[] items()
		{
			return new MenuItem[] { openItem(), renameItem(), cloneItem(), deleteItem(),
					SeparatorMenuItemBuilder.create().build(), createProjectItem() };
		}
		
		private MenuItem deleteItem()
		{
			return MenuItemBuilder.create().text( "Delete" ).id( "project-deleteItem" ).onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					ref.delete( false );
				}
			} ).build();
		}

		private MenuItem cloneItem()
		{
			return MenuItemBuilder.create().text( "Clone" ).id( "project-cloneItem" ).onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					eventFirer.fireEvent( IntentEvent.create( IntentEvent.INTENT_CLONE, ref ) );
				}
			} ).build();
		}

		private MenuItem renameItem()
		{
			return MenuItemBuilder.create().text( "Rename" ).id( "project-renameItem" ).onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					eventFirer.fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, ref ) );
				}
			} ).build();
		}

		private MenuItem openItem()
		{
			return MenuItemBuilder.create().text( "Open" ).id( "project-openItem" ).onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					openProject();
				}
			} ).build();
		}
		
		public void openProject() {
			eventFirer.fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, ref ) );
		}

	}

	public static class SimpleMenuItemHolder
	{
		protected final Node eventFirer;

		SimpleMenuItemHolder( Node eventFirer )
		{
			this.eventFirer = eventFirer;
		}

		public MenuItem[] items()
		{
			return new MenuItem[] { createProjectItem() };
		}

		protected MenuItem createProjectItem()
		{
			return MenuItemBuilder.create().text( "Create project" ).id( "project-createItem" ).onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					eventFirer.fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
				}
			} ).build();
		}

	}

}
