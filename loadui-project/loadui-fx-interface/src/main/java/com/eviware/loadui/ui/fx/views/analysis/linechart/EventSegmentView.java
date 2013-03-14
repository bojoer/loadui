package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;

import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class EventSegmentView extends SegmentView<TestEventSegment>
{
	private final ReadOnlyBooleanProperty isExpandedProperty;

	@FXML
	private MenuButton menuButton;

	public EventSegmentView( TestEventSegment segment, LineChartView lineChartView,
			ReadOnlyBooleanProperty isExpandedProperty )
	{
		super( segment, lineChartView );
		this.isExpandedProperty = isExpandedProperty;
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		super.init();
		segmentLabel.setText( segment.getTypeLabel() + " " + segment.getSourceLabel() );

		segmentLabel.minWidthProperty().bind( Bindings.when( isExpandedProperty ).then( 250 ).otherwise( 180 ) );
		segmentLabel.maxWidthProperty().bind( Bindings.when( isExpandedProperty ).then( 320 ).otherwise( 200 ) );
		
		setMenuItemsFor( menuButton );
	}
}
