package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ChangeListener;
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
						return readOnlyString( FormattingUtils.formatTimeMillis( features.getValue().getTestEvent()
								.getTimestamp() ) );
					}
				} ).build();

		TableColumn<Entry, String> eventTypeColumn = TableColumnBuilder.<Entry, String> create().text( "Event Type" )
				.sortable( false ).minWidth( 100 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return readOnlyString( features.getValue().getTypeLabel() );
					}
				} ).build();

		TableColumn<Entry, String> eventSourceColumn = TableColumnBuilder.<Entry, String> create().text( "Event Source" )
				.sortable( false ).minWidth( 100 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return readOnlyString( features.getValue().getSourceLabel() );
					}
				} ).build();

		TableColumn<Entry, String> descriptionColumn = TableColumnBuilder.<Entry, String> create().text( "Description" )
				.sortable( false ).prefWidth( 300 )
				.cellValueFactory( new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>()
				{
					@Override
					public ObservableValue<String> call( CellDataFeatures<Entry, String> features )
					{
						return readOnlyString( features.getValue().getTestEvent().toString() );
					}
				} ).build();

		getColumns().setAll( timeColumn, eventTypeColumn, eventSourceColumn, descriptionColumn );
	}

	private static ObservableValue<String> readOnlyString( String value )
	{
		return new StaticString( value );
	}

	private static class StaticString extends StringExpression
	{
		private final String value;

		private StaticString( String value )
		{
			this.value = value;
		}

		@Override
		public String get()
		{
			return value;
		}

		@Override
		public void addListener( ChangeListener<? super String> arg0 )
		{
		}

		@Override
		public void removeListener( ChangeListener<? super String> arg0 )
		{
		}

		@Override
		public void addListener( InvalidationListener arg0 )
		{
		}

		@Override
		public void removeListener( InvalidationListener arg0 )
		{
		}
	}
}
