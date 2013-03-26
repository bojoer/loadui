/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.workspace;

import java.util.ArrayList;
import java.util.Collections;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.control.ButtonDialog;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SystemPropertiesDialog extends ButtonDialog
{
	private static final ObservableList<String> properties = FXCollections.observableArrayList();

	public static void initialize()
	{
		ArrayList<String> list = Lists.newArrayList( Iterables.filter( System.getProperties().keySet(), String.class ) );
		Collections.sort( list );
		properties.setAll( list );
	}

	@SuppressWarnings( "unchecked" )
	public SystemPropertiesDialog( Node owner )
	{
		super( owner, "System Properties" );

		setMinWidth( 500 );

		TableColumn<String, String> keyColumn = TableColumnBuilder.<String, String> create().text( "Property" )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<String, String> param )
					{
						return Bindings.format( "%s", param.getValue() );
					}
				} ).build();
		TableColumn<String, String> valueColumn = TableColumnBuilder.<String, String> create().text( "Value" )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<String, String> param )
					{
						return Bindings.format( "%s", System.getProperty( param.getValue() ) );
					}
				} ).build();

		getItems().setAll(
				TableViewBuilder.<String> create().columns( keyColumn, valueColumn ).items( properties ).build() );

		getButtons().setAll( ButtonBuilder.create().text( "Copy to Clipboard" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				StringBuilder stringBuilder = new StringBuilder();
				String lineSeparator = "\n";

				for( String propertyKey : properties )
				{
					stringBuilder.append( propertyKey ).append( ": " ).append( System.getProperty( propertyKey ) )
							.append( lineSeparator );
				}

				ClipboardContent content = new ClipboardContent();
				content.put( DataFormat.PLAIN_TEXT, stringBuilder.toString() );
				Clipboard.getSystemClipboard().setContent( content );
			}
		} ).build(), ButtonBuilder.create().text( "Close" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
			}
		} ).build() );
	}
}
