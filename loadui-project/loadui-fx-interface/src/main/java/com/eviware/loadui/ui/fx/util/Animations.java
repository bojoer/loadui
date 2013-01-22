package com.eviware.loadui.ui.fx.util;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
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
		HIDDEN, SLIDING_DOWN, SLIDING_UP, FADING_AWAY, VISIBLE
	}

	private static final Duration DEFAULT_SLIDE_DOWN_DURATION = Duration.millis( 500 );
	private static final Duration DEFAULT_SLIDE_UP_DURATION = Duration.millis( 500 );
	private static final Duration DEFAULT_FADE_AWAY_DURATION = Duration.seconds( 1 );

	private final TranslateTransition SLIDE_DOWN;
	private final TranslateTransition SLIDE_UP;
	private final FadeTransition FADE_AWAY;
	private State state;
	private Node toAnimate;

	public Animations( Node toAnimate, boolean initiallyVisible )
	{
		this( toAnimate, initiallyVisible, DEFAULT_SLIDE_DOWN_DURATION, DEFAULT_SLIDE_UP_DURATION,
				DEFAULT_FADE_AWAY_DURATION );
	}

	public Animations( Node toAnimate, boolean initiallyVisible, Duration slideDown, Duration slideUp, Duration fadeAway )
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

	public void slideDown()
	{
		stopAnyRunningAnimation();
		toAnimate.setVisible( true );
		state = State.SLIDING_DOWN;
		SLIDE_DOWN.playFromStart();
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

}
