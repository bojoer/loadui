package com.eviware.loadui.ui.fx.control;

import java.util.Comparator;
import java.util.TreeMap;

import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

@DefaultProperty( "items" )
public class ToolBox extends VBox
{
	private static final String TOOL_BOX_PROPERTY = "tool-box";
	private static final String CATEGORY_PROPERTY = "tool-box-category";
	private static final String DEFAULT_STYLE_CLASS = "tool-box";

	public static void setCategory( Node node, String category )
	{
		node.getProperties().put( CATEGORY_PROPERTY, category );
		Object toolBox = node.getProperties().get( TOOL_BOX_PROPERTY );
		if( toolBox instanceof ToolBox )
		{
			( ( ToolBox )toolBox ).refreshItems();
		}
	}

	public static String getCategory( Node node )
	{
		Object category = node.getProperties().get( CATEGORY_PROPERTY );
		return String.valueOf( category == null ? node : category );
	}

	private final Label title = new Label( "Toolbox" );
	private final VBox contentBox = new VBox();
	private final ObservableList<Node> items = FXCollections.observableArrayList();
	private TreeMap<String, ToolBoxCategory> categories = new TreeMap<>();

	public ToolBox( String title )
	{
		this();
		setText( title );
	}

	public ToolBox()
	{
		getStyleClass().add( DEFAULT_STYLE_CLASS );

		items.addListener( new ListChangeListener<Node>()
		{
			@Override
			public void onChanged( ListChangeListener.Change<? extends Node> change )
			{
				refreshItems();
			}
		} );

		refreshItems();

		title.getStyleClass().add( "title" );
		getChildren().setAll( title, contentBox );
	}

	public void setCategoryComparator( Comparator<String> comparator )
	{
		TreeMap<String, ToolBoxCategory> newCategories = new TreeMap<>( comparator );
		newCategories.putAll( categories );
		categories = newCategories;
		refreshItems();
	}

	public void setText( String label )
	{
		title.setText( label );
	}

	public ObservableList<Node> getItems()
	{
		return items;
	}

	private void refreshItems()
	{
		contentBox.getChildren().setAll( new Separator() );
		categories.clear();
		for( Node node : items )
		{
			node.getProperties().put( TOOL_BOX_PROPERTY, this );

			String category = getCategory( node );
			ToolBoxCategory toolBoxCategory = null;
			if( !categories.containsKey( category ) )
			{
				categories.put( category, toolBoxCategory = new ToolBoxCategory( category ) );
			}
			else
			{
				toolBoxCategory = categories.get( category );
			}
			toolBoxCategory.items.add( node );
		}

		for( ToolBoxCategory category : categories.values() )
		{
			category.refresh();
			contentBox.getChildren().addAll( category, new Separator() );
		}
	}

	private class ToolBoxCategory extends BorderPane
	{
		private final ObservableList<Node> items = FXCollections.observableArrayList();
		private final VBox vbox;
		private final Label label;
		private final String category;
		private final Button expanderButton;

		private ToolBoxCategory( String category )
		{
			getStyleClass().setAll( "tool-box-category" );
			this.category = category;
			label = LabelBuilder.create().text( category ).build();
			vbox = new VBox();

			expanderButton = ButtonBuilder.create().id( "expanderButton" ).build();
			setAlignment( expanderButton, Pos.CENTER_RIGHT );
			setRight( expanderButton );
			setLeft( vbox );
		}

		private void refresh()
		{
			expanderButton.setDisable( items.size() < 2 );
			vbox.getChildren().setAll( label, items.get( 0 ) );
		}
	}
}
