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
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.ui.fx.util.Animations;
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

	public NotificationPanel()
	{
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		System.out.println( "Initializing" );
		log.debug( "Initializing" );
		setVisible( false );
	}

	@FXML
	private void showSystemLog( MouseEvent event )
	{
		System.out.println( "Showing system log" );
		log.debug( "Showing system log" );
	}

	@FXML
	private void hideNotifications( MouseEvent event )
	{
		System.out.println( "Hiding notifications" );
		log.debug( "Hiding notifications" );
		anime.slideUp();
	}

	public Label getMsgText()
	{
		return msgText;
	}

	@Override
	public void onTestEvent( Entry event )
	{
		if( event.getTestEvent() instanceof MessageTestEvent )
		{
			detectMouseMovement( false );
			final MessageTestEvent te = ( MessageTestEvent )event.getTestEvent();
			final String dateStr = dateFormat.format( new Date() );
			System.out.println( "Got a MessageTestEvent: " + te.getMessage() );
			log.debug( "Got a MessageTestEvent: " + te.getMessage() );
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					msgText.setText( te.getMessage() );
					dateText.setText( dateStr );
					display();
				}
			} );
		}
		else
		{
			System.out.println( "Got an event of wrong type: " + event.getTestEvent().getClass() );
			log.debug( "Got an event of wrong type: " + event.getTestEvent().getClass() );
		}

	}

	private void display()
	{
		if( fadeAwayTask != null )
			fadeAwayTask.cancel();
		anime.slideDown();
		detectMouseMovement( true );
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
