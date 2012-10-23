package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
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
		TableColumn<Entry, String> timeColumn = TableColumnBuilder.<Entry, String> create().text( "Time" )
				.sortable( false ).minWidth( 80 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( FormattingUtils.formatTimeMillis( features.getValue()
								.getTestEvent().getTimestamp() ) );
					}
				} ).build();

		TableColumn<Entry, String> eventTypeColumn = TableColumnBuilder.<Entry, String> create().text( "Event Type" )
				.sortable( false ).minWidth( 100 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getTypeLabel() );
					}
				} ).build();

		TableColumn<Entry, String> eventSourceColumn = TableColumnBuilder.<Entry, String> create().text( "Event Source" )
				.sortable( false ).minWidth( 100 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getSourceLabel() );
					}
				} ).build();

		TableColumn<Entry, String> descriptionColumn = TableColumnBuilder.<Entry, String> create().text( "Description" )
				.sortable( false ).prefWidth( 300 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return new ReadOnlyStringWrapper( features.getValue().getTestEvent().toString() );
					}
				} ).build();

		getColumns().setAll( timeColumn, eventTypeColumn, eventSourceColumn, descriptionColumn );
	}
}
