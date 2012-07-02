package com.eviware.loadui.ui.fx.util;

import java.lang.ref.WeakReference;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringPropertyBuilder;
import javafx.beans.value.ObservableStringValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Labeled;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Utility methods for working with JavaFX Properties.
 * 
 * @author dain.nilsson
 */
public class Properties
{
	//Used to keep weak listeners alive as long as the key object is in use.
	private static final Cache<Object, EventHandler<?>> listeners = CacheBuilder.newBuilder().weakKeys().build();

	/**
	 * Creates an ObservableStringValue for a Labeled. If the Labeled is mutable,
	 * the returned value will also implement Property<String>.
	 * 
	 * @param labeled
	 * @return
	 */
	public static ObservableStringValue forLabel( @Nonnull Labeled labeled )
	{
		return forBaseEvent( labeled, "label", Labeled.LABEL, labeled instanceof Labeled.Mutable );
	}

	/**
	 * Creates an ObservableStringValue for a Describable. If the Describable is
	 * mutable, the returned value will also implement Property<String>.
	 * 
	 * @param describable
	 * @return
	 */
	public static ObservableStringValue forDescription( @Nonnull Describable describable )
	{
		return forBaseEvent( describable, "description", Describable.DESCRIPTION,
				describable instanceof Describable.Mutable );
	}

	/**
	 * Creates an ObservableStringValue for an Object. If the mutable parameter
	 * is true, the returned value will also implement Property<String>.
	 * 
	 * @param bean
	 *           The Java Bean to create the value for.
	 * @param name
	 *           The name of the property, should correspond to a getter and, if
	 *           mutable, a setter method available on bean.
	 * @param eventKey
	 *           An optional BaseEvent key, which, when fired from the bean
	 *           (which needs to implement EventFirer), will update the value.
	 * @param mutable
	 *           If the value can be set, or not.
	 * @return
	 */
	public static ObservableStringValue forBaseEvent( @Nonnull Object bean, @Nonnull String name,
			@Nullable String eventKey, boolean mutable )
	{
		try
		{
			ObservableStringValue stringProperty;

			if( mutable )
			{
				stringProperty = JavaBeanStringPropertyBuilder.create().bean( bean ).name( "label" ).build();
			}
			else
			{
				stringProperty = ReadOnlyJavaBeanStringPropertyBuilder.create().bean( bean ).name( "label" ).build();
			}

			if( eventKey != null && bean instanceof EventFirer )
			{
				@SuppressWarnings( "unchecked" )
				EventHandler<BaseEvent> eventListener = new BaseEventListener(
						( ReadOnlyJavaBeanProperty<String> )stringProperty, eventKey );
				( ( EventFirer )bean ).addEventListener( BaseEvent.class, eventListener );
				listeners.put( stringProperty, eventListener );
			}

			return stringProperty;
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Creates an Observable from an EventFirer and an event key.
	 * 
	 * @param source
	 * @param key
	 * @return
	 */
	public static Observable observeEvent( final EventFirer source, final String key )
	{
		return new ObservableBase()
		{
			private final Runnable invalidate = new Runnable()
			{
				@Override
				public void run()
				{
					fireInvalidation();
				}
			};

			private final WeakEventHandler<BaseEvent> handler = new WeakEventHandler<BaseEvent>()
			{
				@Override
				public void handleEvent( BaseEvent event )
				{
					if( key.equals( event.getKey() ) )
					{
						Platform.runLater( invalidate );
					}
				}
			};

			{
				source.addEventListener( BaseEvent.class, handler );
			}
		};

	}

	private static final class BaseEventListener implements WeakEventHandler<BaseEvent>
	{
		private final String eventKey;
		private final WeakReference<ReadOnlyJavaBeanProperty<String>> ref;

		private BaseEventListener( ReadOnlyJavaBeanProperty<String> property, String eventKey )
		{
			ref = new WeakReference<>( property );
			this.eventKey = eventKey;
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( eventKey.equals( event.getKey() ) )
			{
				final ReadOnlyJavaBeanProperty<String> property = ref.get();
				if( property != null )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							property.fireValueChangedEvent();
						}
					} );
				}
			}
		}
	}
}
