package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
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

	public ToolbarCounterDisplay( @Nonnull String name, @Nonnull Formatting formatting )
	{
		this.formatting = formatting;
		numberDisplay = numberDisplay();

		progress = progressBar();
		Label label = label( name );

		HBox labelAndProgress = HBoxBuilder.create().children( label, progress ).spacing( 3 ).alignment( Pos.BOTTOM_LEFT )
				.build();

		getChildren().setAll( numberDisplay, labelAndProgress );
		setPadding( new Insets( 0, 3, 0, 3 ) );
		setAlignment( Pos.CENTER );
		setLimit( limit );
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
			progress.setVisible( limit != -1 );
		}
	}

	private static ProgressBar progressBar()
	{
		return ProgressBarBuilder.create().maxWidth( 60 ).style( "-fx-scale-y: 0.6; " ).visible( false ).build();
	}
}
