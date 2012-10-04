package com.eviware.loadui.ui.fx.views.canvas.component;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;

import org.tbee.javafx.scene.layout.MigPane;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.layout.LabelLayoutComponent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SeparatorLayoutComponent;
import com.eviware.loadui.api.layout.TableLayoutComponent;

public class ComponentLayoutUtils
{
	public static Node instantiateLayout( LayoutComponent component )
	{
		if( component instanceof LayoutContainer )
		{
			LayoutContainer container = ( LayoutContainer )component;
			MigPane pane = new MigPane( container.getLayoutConstraints(), container.getColumnConstraints(),
					container.getRowConstraints() );
			for( LayoutComponent child : container )
			{
				pane.add( instantiateLayout( child ), child.getConstraints() );
			}
			return pane;
		}
		else if( component instanceof ActionLayoutComponent )
		{
			ActionLayoutComponent action = ( ActionLayoutComponent )component;
			//TODO: Add enabled listener, fire action, etc.
			return ButtonBuilder.create().text( action.getLabel() ).build();
		}
		else if( component instanceof LabelLayoutComponent )
		{
			return new Label( ( ( LabelLayoutComponent )component ).getLabel() );
		}
		else if( component instanceof PropertyLayoutComponent )
		{
			//TODO: Make editable Property
			return new Label( ( ( PropertyLayoutComponent<?> )component ).getLabel() );
		}
		else if( component instanceof SeparatorLayoutComponent )
		{
			SeparatorLayoutComponent separator = ( SeparatorLayoutComponent )component;
			return new Separator( separator.isVertical() ? Orientation.VERTICAL : Orientation.HORIZONTAL );
		}
		else if( component instanceof TableLayoutComponent )
		{
			//TODO: Table stuff
			return new TableView<>();
		}
		else
		{
			return new Label( "Unhandled: " + component );
		}
	}
}
