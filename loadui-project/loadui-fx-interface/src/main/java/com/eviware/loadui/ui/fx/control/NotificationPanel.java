package com.eviware.loadui.ui.fx.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.ui.fx.util.Animations;
import com.eviware.loadui.ui.fx.util.Animations.State;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.util.testevents.MessageTestEvent;

public class NotificationPanel extends VBox implements TestEventObserver, EventHandler<MouseEvent>
{

	private static final Logger log = LoggerFactory.getLogger( NotificationPanel.class );
	private static final DateFormat dateFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss", Locale.ENGLISH );

	private Animations anime = new Animations( this, false );
	private Timer delayer = new Timer( "NotificationsPanel-Timer", true );
	private TimerTask fadeAwayTask;

	@FXML
	private Label dateText;

	@FXML
	private Label msgCount;

	@FXML
	private Label msgText;

	private Pane originalParent;

	private EventHandler<MouseEvent> showLogHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle( MouseEvent arg0 )
		{
			// no default action
		}
	};

	public NotificationPanel()
	{
		FXMLUtils.load( this );
	}

	public void setMainWindowView( Pane mainView )
	{
		this.originalParent = mainView;
	}

	public void setOnShowLog( EventHandler<MouseEvent> handler )
	{
		showLogHandler = handler;
	}

	@FXML
	private void initialize()
	{
		log.debug( "Initializing" );
		setVisible( false );
		setOnMouseEntered( new EventHandler<MouseEvent>()
		{

			@Override
			public void handle( MouseEvent event )
			{
				State state = anime.getCurrentState();
				if( state == State.VISIBLE || state == State.FADING_AWAY )
				{
					anime.stopAnyRunningAnimation();
					if( fadeAwayTask != null )
						fadeAwayTask.cancel();
				}
			}

		} );
		setOnMouseExited( new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( anime.getCurrentState() == State.VISIBLE )
				{
					anime.fadeAway();
				}
			}
		} );
	}

	@FXML
	private void showSystemLog( MouseEvent event )
	{
		showLogHandler.handle( event );
	}

	@FXML
	private void hideNotifications( MouseEvent event )
	{
		anime.slideUp();
	}

	public Label getMsgText()
	{
		return msgText;
	}

	@Override
	public void onTestEvent( Entry entry )
	{
		if( entry.getTestEvent() instanceof MessageTestEvent )
		{
			final MessageTestEvent te = ( MessageTestEvent )entry.getTestEvent();
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					if( isVisible() )
					{
						msgCount.setText( Integer.toString( getCurrentMsgCount() + 1 ) );
					}
					else
					{
						msgCount.setText( "" );
						detectMouseMovement( false );
						final String dateStr = dateFormat.format( new Date() );
						msgText.setText( te.getMessage() );
						dateText.setText( dateStr );
						display();
					}
				}
			} );
		}
	}

	private Integer getCurrentMsgCount()
	{
		String text = msgCount.getText().isEmpty() ? "0" : msgCount.getText();
		return Integer.valueOf( text );
	}

	private void display()
	{
		ensureThisIsUnderPreferredParent();

		if( fadeAwayTask != null )
			fadeAwayTask.cancel();

		// must run this only after the JavaFX processes the above code so the panel will be in
		// the correct place before sliding down
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				anime.slideDown();
				detectMouseMovement( true );
			}
		} );

	}

	private void ensureThisIsUnderPreferredParent()
	{
		if( originalParent == null )
		{
			log.debug( "No mainView has been set, we need to set it to something like its current parent" );
			originalParent = ( Pane )getParent();
		}

		// The preferred place for the NotificationPanel is in the first Pane of the DOM
		Pane pane = ( Pane )originalParent.getScene().lookup( "Pane" );

		if( pane == null )
		{
			if( getParent() != originalParent )
			{
				originalParent.getChildren().add( this );
			}
		}
		else if( pane != getParent() )
		{
			pane.getChildren().add( this );
		}
	}

	private void detectMouseMovement( boolean enable )
	{
		if( enable )
			getParent().addEventHandler( MouseEvent.MOUSE_MOVED, this );
		else
			getParent().removeEventHandler( MouseEvent.MOUSE_MOVED, this );
	}

	@Override
	public void handle( MouseEvent event )
	{
		detectMouseMovement( false );
		fadeAwayTask = new TimerTask()
		{
			@Override
			public void run()
			{
				anime.fadeAway();
			}
		};

		delayer.schedule( fadeAwayTask, 3000 );
	}

}
