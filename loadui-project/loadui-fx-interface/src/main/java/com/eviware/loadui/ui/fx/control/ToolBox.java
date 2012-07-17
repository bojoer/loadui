package com.eviware.loadui.ui.fx.control;

import java.util.Comparator;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

@DefaultProperty( "items" )
public class ToolBox<E extends Node> extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "tool-box";
	private static final String TOOL_BOX_PROPERTY = "tool-box";
	private static final String CATEGORY_PROPERTY = "tool-box-category";

	/**
	 * Sets the category for a child when contained by a ToolBox. When no
	 * category has been set, or it has been explicitly set to null, the child's
	 * toString() value will be used as a category.
	 * 
	 * @param node
	 * @param category
	 */
	public static void setCategory( Node node, String category )
	{
		node.getProperties().put( CATEGORY_PROPERTY, category );
		Object toolBox = node.getProperties().get( TOOL_BOX_PROPERTY );
		if( toolBox instanceof ToolBox )
		{
			//( ( ToolBox<?> )toolBox ).refreshItems();
		}
	}

	/**
	 * Returns the child's Category.
	 * 
	 * @param node
	 * @return
	 */
	public static String getCategory( Node node )
	{
		Object category = node.getProperties().get( CATEGORY_PROPERTY );
		return String.valueOf( category == null ? node : category );
	}

	private final Label label;
	private final ObservableList<E> items = FXCollections.observableArrayList();
	private final ObjectProperty<Comparator<String>> categoryComparator = new ObjectPropertyBase<Comparator<String>>()
	{
		@Override
		public Object getBean()
		{
			return this;
		}

		@Override
		public String getName()
		{
			return "categoryComparator";
		}
	};

	public ToolBox()
	{
		this.label = new Label();
		initialize();
	}

	public ToolBox( String label )
	{
		this.label = new Label( label );
		initialize();
	}

	public ToolBox( String label, Node graphic )
	{
		this.label = new Label( label, graphic );
		initialize();
	}

	public void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public Label getLabel()
	{
		return label;
	}

	public ObservableList<E> getItems()
	{
		return items;
	}

	public ObjectProperty<Comparator<String>> categoryComparatorProperty()
	{
		return categoryComparator;
	}

	public void setCategoryComparator( Comparator<String> categoryComparator )
	{
		this.categoryComparator.set( categoryComparator );
	}

	public Comparator<String> getCategoryComparator()
	{
		return categoryComparator.get();
	}
}
