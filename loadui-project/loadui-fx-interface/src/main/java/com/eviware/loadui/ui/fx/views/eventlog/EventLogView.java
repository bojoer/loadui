package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.util.FormattingUtils;

public class EventLogView extends TableView<TestEvent.Entry>
{
	@SuppressWarnings( "unchecked" )
	public EventLogView()
	{
		TableColumn<Entry, String> timeColumn = new TableColumn<>( "Time" );
		timeColumn
				.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( FormattingUtils.formatTimeMillis( features.getValue()
								.getTestEvent().getTimestamp() ) );
					}
				} );

		TableColumn<Entry, String> eventTypeColumn = new TableColumn<>( "Event Type" );
		eventTypeColumn
				.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getTypeLabel() );
					}
				} );

		TableColumn<Entry, String> eventSourceColumn = new TableColumn<>( "Event Source" );
		eventTypeColumn
				.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getSourceLabel() );
					}
				} );

		TableColumn<Entry, String> descriptionColumn = new TableColumn<>( "Description" );
		eventTypeColumn
				.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getTestEvent().toString() );
					}
				} );

		getColumns().setAll( timeColumn, eventTypeColumn, eventSourceColumn, descriptionColumn );
	}
}
