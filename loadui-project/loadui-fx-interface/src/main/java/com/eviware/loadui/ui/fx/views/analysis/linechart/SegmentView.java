package com.eviware.loadui.ui.fx.views.analysis.linechart;

import java.util.ArrayList;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.util.statistics.ChartUtils;

public abstract class SegmentView<T extends Segment> extends StackPane implements Deletable
{
	public static final String COLOR_ATTRIBUTE = "color";
	protected static final Logger log = LoggerFactory.getLogger( SegmentView.class );

	protected final T segment;
	protected final LineChartView lineChartView;

	@FXML
	protected Label segmentLabel;

	@FXML
	protected Rectangle legendColorRectangle;

	protected String color;

	public SegmentView( T segment, LineChartView lineChartView )
	{
		this.segment = segment;
		this.lineChartView = lineChartView;
		color = segment.getAttribute( COLOR_ATTRIBUTE, "no_color" );
	}

	private String newColor()
	{
		LineChartView mainChart = ( LineChartView )( lineChartView.getChartGroup().getChartView() );

		ArrayList<String> currentColorList = new ArrayList<>();

		for( Segment s : mainChart.getSegments() )
		{
			currentColorList.add( s.getAttribute( COLOR_ATTRIBUTE, "no_color" ) );
		}

		return ChartUtils.getNewRandomColor( currentColorList );
	}

	public void setColor( String color )
	{
		this.color = color;
		legendColorRectangle.setFill( Color.web( color ) );
		segment.setAttribute( COLOR_ATTRIBUTE, color );
	}

	protected void init()
	{
		if( color.equals( "no_color" ) )
			setColor( newColor() );
		else
			legendColorRectangle.setFill( Color.web( color ) );
	}

	/**
	 * Sub-classes may set a default array of MenuItems on the given button with
	 * this method.
	 * 
	 * @param menuButton
	 *           to hold the menu
	 */
	protected void setMenuItemsFor( final MenuButton menuButton )
	{
		MenuItem[] menuItems = MenuItemsProvider.createWith( this, this, Options.are() ).items();
		menuButton.getItems().setAll( menuItems );
		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( menuItems ).build();

		Bindings.bindContentBidirectional( ctxMenu.getItems(), menuButton.getItems() );

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );
	}

	@Override
	public void delete()
	{
		( ( Segment.Removable )segment ).remove();
	}
}
