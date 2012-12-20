package com.eviware.loadui.ui.fx.util;

import java.lang.ref.WeakReference;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanDoublePropertyBuilder;
import javafx.beans.property.adapter.JavaBeanLongPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanPropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanDoublePropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanLongPropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringPropertyBuilder;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Labeled;

/**
 * Utility methods for working with JavaFX Properties.
 * 
 * @author dain.nilsson
 */
public class Properties
{
	public final static ReadOnlyBooleanProperty TRUE = new SimpleBooleanProperty( true );
	public final static ReadOnlyBooleanProperty FALSE = new SimpleBooleanProperty( false );

	/**
	 * Creates an ReadOnlyStringProperty for a Labeled. If the Labeled is
	 * mutable, the returned value will also implement StringProperty.
	 * 
	 * @param labeled
	 * @return
	 */
	public static ReadOnlyStringProperty forLabel( @Nonnull Labeled labeled )
	{
		if( labeled instanceof EventFirer )
		{
			EventFirer eventFirer = ( EventFirer )labeled;
			return labeled instanceof Labeled.Mutable ? stringProperty( eventFirer, "label", Labeled.LABEL )
					: readOnlyStringProperty( eventFirer, "label", Labeled.LABEL );
		}
		try
		{
			return labeled instanceof Labeled.Mutable ? JavaBeanStringPropertyBuilder.create().bean( labeled )
					.name( "label" ).build() : ReadOnlyJavaBeanStringPropertyBuilder.create().bean( labeled ).name( "label" )
					.build();
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	public static StringProperty forLabel( @Nonnull Labeled.Mutable labeled )
	{
		return ( StringProperty )forLabel( ( Labeled )labeled );
	}

	/**
	 * Creates an ReadOnlyStringProperty for a Describable. If the Describable is
	 * mutable, the returned value will also implement StringProperty.
	 * 
	 * @param describable
	 * @return
	 */
	public static ReadOnlyStringProperty forDescription( @Nonnull Describable describable )
	{
		if( describable instanceof EventFirer )
		{
			EventFirer eventFirer = ( EventFirer )describable;
			return describable instanceof Describable.Mutable ? stringProperty( eventFirer, "description",
					Describable.DESCRIPTION ) : readOnlyStringProperty( eventFirer, "description", Describable.DESCRIPTION );
		}
		try
		{
			return describable instanceof Describable.Mutable ? JavaBeanStringPropertyBuilder.create().bean( describable )
					.name( "description" ).build() : ReadOnlyJavaBeanStringPropertyBuilder.create().bean( describable )
					.name( "description" ).build();
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	public static StringProperty forDescription( @Nonnull Describable.Mutable describable )
	{
		return ( StringProperty )forDescription( ( Describable )describable );
	}

	/**
	 * Converts a loadUI Property into a JavaFX 2 Property, with two-way
	 * listeners.
	 * 
	 * @param loadUIProperty
	 * @return
	 */
	public static <T> Property<T> convert( final com.eviware.loadui.api.property.Property<T> loadUIProperty )
	{
		return new LoadUIPropertyAdapter<>( loadUIProperty );
	}

	/**
	 * Returns a StringProperty for a standard Java getter/setter pair for an
	 * EventFirer, firing a BaseEvent for the given key whenever the value is
	 * changed.
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static StringProperty stringProperty( @Nonnull EventFirer bean, @Nonnull String name, @Nonnull String eventKey )
	{
		try
		{
			return withListener( JavaBeanStringPropertyBuilder.create().bean( bean ).name( name ).build(), bean, eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Read-only version of stringProperty.
	 * 
	 * @see stringProperty
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static ReadOnlyStringProperty readOnlyStringProperty( @Nonnull EventFirer bean, @Nonnull String name,
			@Nonnull String eventKey )
	{
		try
		{
			return withListener( ReadOnlyJavaBeanStringPropertyBuilder.create().bean( bean ).name( name ).build(), bean,
					eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Returns a BooleanProperty for a standard Java getter/setter pair for an
	 * EventFirer, firing a BaseEvent for the given key whenever the value is
	 * changed.
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static BooleanProperty booleanProperty( @Nonnull EventFirer bean, @Nonnull String name,
			@Nonnull String eventKey )
	{
		try
		{
			return withListener( JavaBeanBooleanPropertyBuilder.create().bean( bean ).name( name ).build(), bean, eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Read-only version of booleanProperty.
	 * 
	 * @see booleanProperty
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static ReadOnlyBooleanProperty readOnlyBooleanProperty( @Nonnull EventFirer bean, @Nonnull String name,
			@Nonnull String eventKey )
	{
		try
		{
			return withListener( ReadOnlyJavaBeanBooleanPropertyBuilder.create().bean( bean ).name( name ).build(), bean,
					eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Returns a LongProperty for a standard Java getter/setter pair for an
	 * EventFirer, firing a BaseEvent for the given key whenever the value is
	 * changed.
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static LongProperty longProperty( @Nonnull EventFirer bean, @Nonnull String name, @Nonnull String eventKey )
	{
		try
		{
			return withListener( JavaBeanLongPropertyBuilder.create().bean( bean ).name( name ).build(), bean, eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Read-only version of longProperty.
	 * 
	 * @see booleanProperty
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static ReadOnlyLongProperty readOnlyLongProperty( @Nonnull EventFirer bean, @Nonnull String name,
			@Nonnull String eventKey )
	{
		try
		{
			return withListener( ReadOnlyJavaBeanLongPropertyBuilder.create().bean( bean ).name( name ).build(), bean,
					eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Returns a DoubleProperty for a standard Java getter/setter pair for an
	 * EventFirer, firing a BaseEvent for the given key whenever the value is
	 * changed.
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static DoubleProperty doubleProperty( @Nonnull EventFirer bean, @Nonnull String name, @Nonnull String eventKey )
	{
		try
		{
			return withListener( JavaBeanDoublePropertyBuilder.create().bean( bean ).name( name ).build(), bean, eventKey );
		}
		catch( NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Read-only version of doubleProperty.
	 * 
	 * @see booleanProperty
	 * 
	 * @param bean
	 * @param name
	 * @param eventKey
	 * @return
	 */
	public static ReadOnlyDoubleProperty readOnlyDoubleProperty( @Nonnull EventFirer bean, @Nonnull String name,
			@Nonnull String eventKey )
	{
		try
		{
			return withListener( ReadOnlyJavaBeanDoublePropertyBuilder.create().bean( bean ).name( name ).build(), bean,
					eventKey );
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

	private static <T extends ReadOnlyJavaBeanProperty<?>> T withListener( T property, EventFirer bean, String eventKey )
	{
		EventHandler<BaseEvent> eventListener = new BaseEventListener( property, eventKey, bean );
		bean.addEventListener( BaseEvent.class, eventListener );

		return property;
	}

	private static final class LoadUIPropertyAdapter<T> extends ObjectPropertyBase<T>
	{
		private final com.eviware.loadui.api.property.Property<T> loadUIProperty;
		private final Runnable setValueRunnable;
		
		private final EventHandler<PropertyEvent> eventHandler = new WeakEventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( event.getProperty() == loadUIProperty )
				{
					Platform.runLater( setValueRunnable );
				}
			}
		};

		private LoadUIPropertyAdapter( com.eviware.loadui.api.property.Property<T> loadUIProperty )
		{
			this.loadUIProperty = loadUIProperty;
			loadUIProperty.getOwner().addEventListener( PropertyEvent.class, eventHandler );
			super.set( loadUIProperty.getValue() );
			setValueRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					setValue( LoadUIPropertyAdapter.this.loadUIProperty.getValue() );
				}
			};
		}

		@Override
		public void set( T value )
		{
			super.set( value );
			loadUIProperty.setValue( value );
		}

		@Override
		public Object getBean()
		{
			return loadUIProperty.getOwner();
		}

		@Override
		public String getName()
		{
			return loadUIProperty.getKey();
		}
	}

	private static final class BaseEventListener implements EventHandler<BaseEvent>
	{
		private final String eventKey;
		private final EventFirer eventFirer;
		private final WeakReference<ReadOnlyJavaBeanProperty<?>> ref;

		private BaseEventListener( ReadOnlyJavaBeanProperty<?> property, String eventKey, EventFirer eventFirer )
		{
			ref = new WeakReference<ReadOnlyJavaBeanProperty<?>>( property );
			this.eventKey = eventKey;
			this.eventFirer = eventFirer;
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( eventKey.equals( event.getKey() ) )
			{
				final ReadOnlyJavaBeanProperty<?> property = ref.get();
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
				else
				{
					eventFirer.removeEventListener( BaseEvent.class, this );
				}
			}
		}
	}
}
