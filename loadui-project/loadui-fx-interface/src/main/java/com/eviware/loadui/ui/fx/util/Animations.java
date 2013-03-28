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
package com.eviware.loadui.ui.fx.util;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Animations
{

	public enum State
	{
		HIDDEN, SLIDING_DOWN, SLIDING_UP, FADING_IN, FADING_AWAY, VISIBLE
	}

	private static final Duration DEFAULT_SLIDE_DOWN_DURATION = Duration.millis( 500 );
	private static final Duration DEFAULT_SLIDE_UP_DURATION = Duration.millis( 500 );
	private static final Duration DEFAULT_FADE_IN_DURATION = Duration.millis( 250 );
	private static final Duration DEFAULT_FADE_AWAY_DURATION = Duration.seconds( 1 );
	
	private final TranslateTransition SLIDE_DOWN;
	private final TranslateTransition SLIDE_UP;
	private final FadeTransition FADE_AWAY;
	private final FadeTransition FADE_IN;
	private State state;
	private Node toAnimate;

	public Animations( Node toAnimate, boolean initiallyVisible )
	{
		this( toAnimate, initiallyVisible, DEFAULT_SLIDE_DOWN_DURATION, DEFAULT_SLIDE_UP_DURATION,
				DEFAULT_FADE_IN_DURATION, DEFAULT_FADE_AWAY_DURATION );
	}

	public Animations( Node toAnimate, boolean initiallyVisible, Duration slideDown, Duration slideUp, Duration fadeIn, Duration fadeAway )
	{
		this.toAnimate = toAnimate;
		toAnimate.setVisible( initiallyVisible );
		state = initiallyVisible ? State.VISIBLE : State.HIDDEN;

		SLIDE_DOWN = TranslateTransitionBuilder.create().fromY( -200 ).toY( 0 ).node( toAnimate ).duration( slideDown )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						toVisibleState();
					}
				} ).build();

		SLIDE_UP = TranslateTransitionBuilder.create().fromY( 0 ).toY( -200 ).node( toAnimate ).duration( slideUp )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						toHiddenState();
					}
				} ).build();

		FADE_AWAY = FadeTransitionBuilder.create().node( toAnimate ).fromValue( 1 ).toValue( 0 ).duration( fadeAway )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						toHiddenState();
					}
				} ).build();
		FADE_IN = FadeTransitionBuilder.create().node( toAnimate ).fromValue( 0 ).toValue( 1 ).duration( fadeIn )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						toVisibleState();
					}
				} ).build();
	}

	public State getCurrentState()
	{
		return state;
	}

	public State stopAnyRunningAnimation()
	{
		switch( state )
		{
		case SLIDING_DOWN :
			stop( SLIDE_DOWN );
			state = State.HIDDEN;
			return state;
		case VISIBLE :
			return state;
		case FADING_AWAY :
			stop( FADE_AWAY );
			state = State.VISIBLE;
			return state;
		case HIDDEN :
		default :
			return state;
		case SLIDING_UP :
		case FADING_IN:
			stop( SLIDE_UP );
			state = State.VISIBLE;
			return state;
		}
	}

	private void stop( Animation animation )
	{
		animation.jumpTo( Duration.ZERO );
		animation.stop();
	}

	public void fadeAway()
	{
		if( stopAnyRunningAnimation() == State.VISIBLE )
		{
			toAnimate.setVisible( true );
			state = State.FADING_AWAY;
			FADE_AWAY.playFromStart();
		}
	}
	
	public void fadeIn()
	{
		if( stopAnyRunningAnimation() == State.HIDDEN )
		{
			toAnimate.setVisible( true );
			state = State.FADING_IN;
			FADE_IN.playFromStart();
		}
	}

	public Then slideDown()
	{
		stopAnyRunningAnimation();
		toAnimate.setVisible( true );
		state = State.SLIDING_DOWN;
		SLIDE_DOWN.playFromStart();
		return new Then( SLIDE_DOWN );
	}

	public void slideUp()
	{
		if( stopAnyRunningAnimation() == State.VISIBLE )
		{
			toAnimate.setVisible( true );
			state = State.SLIDING_UP;
			SLIDE_UP.playFromStart();
		}
	}

	private void toHiddenState()
	{
		state = State.HIDDEN;
		toAnimate.setVisible( false );
		toAnimate.setOpacity( 1.0 );
	}

	private void toVisibleState()
	{
		state = State.VISIBLE;
		toAnimate.setVisible( true );
	}

	public class Then
	{

		private final Transition transition;
		private final List<Runnable> runnables = new ArrayList<>();
		private FinishedListener listener;
		private EventHandler<ActionEvent> prevHandler;

		public Then( TranslateTransition transition )
		{
			this.transition = transition;
		}

		public Then then( final Runnable action )
		{
			if( listener == null )
			{
				listener = new FinishedListener();
				prevHandler = transition.getOnFinished();
				transition.setOnFinished( listener );
			}
			runnables.add( action );
			return this;
		}

		private class FinishedListener implements EventHandler<ActionEvent>
		{

			@Override
			public void handle( ActionEvent event )
			{
				try
				{
					for( Runnable action : runnables )
						action.run();
				}
				finally
				{
					if( prevHandler != null )
						prevHandler.handle( event );
					transition.setOnFinished( prevHandler );
				}
			}

		}

	}

}
