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
package com.eviware.loadui.ui.fx.views.analysis.linechart;

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

		prefWidthProperty().bind( segmentLabel.widthProperty().add( 85 ) );
		minWidthProperty().bind( segmentLabel.widthProperty().add( 85 ) );
		segmentLabel.maxWidth( 400 );
		segmentLabel.wrapTextProperty().set( true );

		setMenuItemsFor( menuButton );
	}

}
