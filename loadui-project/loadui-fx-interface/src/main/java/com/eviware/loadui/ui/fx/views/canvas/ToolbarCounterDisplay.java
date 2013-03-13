package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.util.StringUtils;

public class ToolbarCounterDisplay extends CounterDisplay
{
	protected static final Logger log = LoggerFactory.getLogger( ToolbarCounterDisplay.class );

	private long limit;
	private final ProgressBar progress;
	private Label limitDisplay;
	private Label separationSlash; 
	
	public ToolbarCounterDisplay( @Nonnull String name, @Nonnull Formatting formatting )
	{
		this.formatting = formatting;

		numberDisplay = numberDisplay();
		numberDisplay.setAlignment( Pos.CENTER_RIGHT );
		separationSlash = LabelBuilder.create().style( "-fx-text-fill: #f2f2f2; -fx-font-size: 10px;" ).alignment(Pos.CENTER).text( "/" ).build();
		limitDisplay = limitDisplay();
				
		BorderPane numberAndLimitDisplay = BorderPaneBuilder
				.create()
				.prefWidth( 100 )
				.maxWidth( 160)
				.center(HBoxBuilder.create().spacing(2).children(numberDisplay, separationSlash).build())
				.right(limitDisplay)
				.style("-fx-background-color: linear-gradient(to bottom, #545454 0%, #000000 50%, #000000 100%); -fx-padding: 1 2 1 2; -fx-background-radius: 5; -fx-border-width: 1; -fx-border-color: #333333; -fx-border-radius: 4; " )
				.build();
			
		progress = progressBar();
		Label label = label( name );
		
		HBox labelAndProgress = HBoxBuilder.create().children( label, progress ).spacing( 3 ).alignment( Pos.BOTTOM_LEFT )
				.build();

		getChildren().setAll( numberAndLimitDisplay, labelAndProgress );
		setSpacing( 1 );
		setAlignment( Pos.CENTER );
		setLimit( limit );
	}
	
	private Label limitDisplay(){
		return LabelBuilder
				.create()
				.minWidth(44)
				.prefWidth(50)
				.alignment( Pos.CENTER_RIGHT )
				.style("-fx-text-fill: #f2f2f2; -fx-font-size: 10px; ")
				.build();
	}

	public ToolbarCounterDisplay( String name )
	{
		this( name, Formatting.NONE );
	}
	
	@Override
	public void setValue( long value )
	{
		if( formatting == Formatting.TIME )
			numberDisplay.setText( StringUtils.toHhMmSs( value ) );
		else
			numberDisplay.setText( String.valueOf( value ) );
		progress.setProgress( ( double )value / ( double )limit );
	}

	public void setLimit( long newLimit )
	{
		if( newLimit != 0 )
		{
			this.limit = newLimit;
						
			if(formatting == Formatting.TIME){
				limitDisplay.setText(StringUtils.toHhMmSs( limit ) );
			}else if(formatting == Formatting.SUFFIX){
				
			}else{
				limitDisplay.setText(String.valueOf( limit) );
			}		
				
			separationSlash.setVisible( limit != -1 );		
			limitDisplay.setVisible( limit != -1 );
			progress.setVisible( limit != -1 );
		}
	}

	private static ProgressBar progressBar()
	{
		return ProgressBarBuilder.create().maxWidth(70).style( "-fx-scale-y: 0.6; " ).visible( false ).build();
	}
}
