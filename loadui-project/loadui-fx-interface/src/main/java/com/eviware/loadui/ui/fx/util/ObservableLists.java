package com.eviware.loadui.ui.fx.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utility class for dealing with JavaFX ObservableLists.
 * 
 * @author dain.nilsson
 */
public class ObservableLists
{
	/**
	 * Creates a readonly ObservableList containing all OSGi published services
	 * for the given Class type. The list is dynamically updated to reflect
	 * changes in the services.
	 * 
	 * @param type
	 * @return
	 */
	public static <E> ObservableList<E> ofServices( Class<E> type )
	{
		try
		{
			return ofServices( type, "(objectclass=" + type.getName() + ")" );
		}
		catch( InvalidSyntaxException e )
		{
			//This should never happen.
			e.printStackTrace();
			throw new InternalError();
		}
	}

	/**
	 * Creates a readonly ObservableList containing all OSGi published services
	 * for the given Class type, which match the given filter. The list is
	 * dynamically updated to reflect changes in the services.
	 * 
	 * @param type
	 * @return
	 */
	public static <E> ObservableList<E> ofServices( Class<E> type, String filter ) throws InvalidSyntaxException
	{
		BundleContext context = BeanInjector.getBean( BundleContext.class );

		ServiceListData<E> data = new ServiceListData<>( context, type, filter );

		ObservableList<E> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	/**
	 * Creates an ObservableList of elements from an EventFirer firing
	 * CollectionEvents.
	 * 
	 * @param owner
	 * @param collectionName
	 * @param type
	 * @param initialValues
	 * @return
	 */
	public static <E> ObservableList<E> ofCollection( EventFirer owner, String collectionName, Class<E> type,
			Iterable<? extends E> initialValues )
	{
		CollectionListData<E> data = new CollectionListData<>( owner, collectionName, type, initialValues );

		ObservableList<E> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	/**
	 * Creates an ObservableList of transformed elements from a given list.
	 * 
	 * @param original
	 * @param function
	 * @return
	 */
	public static <F, T> ObservableList<T> transform( ObservableList<F> original, Function<F, T> function )
	{
		TransformedListData<F, T> data = new TransformedListData<>( original, function );

		ObservableList<T> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	/**
	 * Creates an ObservableList which contains all elements in the original list
	 * which satisfy the given condition.
	 * 
	 * @param original
	 * @param condition
	 * @return
	 */
	public static <E> ObservableList<E> filter( ObservableList<E> original, Predicate<E> condition )
	{
		FilteredListData<E> data = new FilteredListData<>( original, condition );

		ObservableList<E> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	/**
	 * Returns an unmodifiable view of the given ObservableList, where all
	 * modifications are guaranteed to be done in the FX thread.
	 * 
	 * @param originalList
	 * @return
	 */
	public static <E> ObservableList<E> fx( ObservableList<E> originalList )
	{
		FXObservableListData<E> data = new FXObservableListData<>( originalList );

		ObservableList<E> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	public static <E> ObservableList<E> fromExpression( Callable<Iterable<E>> expression, Observable... observables )
	{
		ExpressionListData<E> data = new ExpressionListData<>( expression, observables );

		ObservableList<E> readOnlyList = FXCollections.unmodifiableObservableList( data.list );
		lists.put( readOnlyList, data );

		return readOnlyList;
	}

	private static final LoadingCache<List<?>, ListChangeListener<?>> contentListeners = CacheBuilder.newBuilder()
			.weakKeys().build( new CacheLoader<List<?>, ListChangeListener<?>>()
			{
				@Override
				@SuppressWarnings( "rawtypes" )
				public ListChangeListener<?> load( final List<?> list ) throws Exception
				{
					return new ListChangeListener()
					{
						@Override
						@SuppressWarnings( "unchecked" )
						public void onChanged( ListChangeListener.Change change )
						{
							while( change.next() )
							{
								list.removeAll( change.getRemoved() );
								list.addAll( change.getAddedSubList() );
							}
						}
					};
				}
			} );

	/**
	 * Like Bindings.bindContent(), but doesn't take ordering into account. This
	 * means that list1 can be reordered after binding, and the content will
	 * still stay in sync with list2.
	 * 
	 * @param list1
	 * @param list2
	 */
	@SuppressWarnings( "unchecked" )
	public static <E> void bindContentUnordered( List<E> list1, ObservableList<? extends E> list2 )
	{
		if( list1 instanceof ObservableList )
		{
			( ( ObservableList<E> )list1 ).setAll( list2 );
		}
		else
		{
			list1.clear();
			list1.addAll( list2 );
		}

		list2.addListener( ( ListChangeListener<? super E> )contentListeners.getUnchecked( list1 ) );
	}

	public static <E> void bindSorted( final List<E> list1, ObservableList<? extends E> list2,
			final Comparator<? super E> comparator )
	{
		bindContentUnordered( list1, list2 );
		InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( list1 instanceof ObservableList )
				{
					FXCollections.sort( ( ObservableList<E> )list1, comparator );
				}
				else
				{
					Collections.sort( list1, comparator );
				}
			}
		};
		list2.addListener( invalidationListener );
		invalidationListener.invalidated( list2 );
	}

	@SuppressWarnings( "unchecked" )
	public static <E> void unbindContent( List<? super E> list1, ObservableList<E> list2 )
	{
		list2.removeListener( ( ListChangeListener<? super E> )contentListeners.getUnchecked( list1 ) );
	}

	private static final Cache<ObservableList<?>, Releasable> lists = CacheBuilder.newBuilder().weakKeys()
			.removalListener( new RemovalListener<ObservableList<?>, Releasable>()
			{
				@Override
				public void onRemoval( RemovalNotification<ObservableList<?>, Releasable> notification )
				{
					ReleasableUtils.release( notification.getValue() );
				}
			} ).build();

	private static class ServiceListData<E> implements ServiceListener, Releasable
	{
		private final ObservableList<E> list = FXCollections.observableArrayList();
		private final BundleContext context;
		private final Class<E> type;

		private ServiceListData( BundleContext context, Class<E> type, String filter ) throws InvalidSyntaxException
		{
			this.context = context;
			this.type = type;

			context.addServiceListener( this, filter );
			//TODO: We can't use generics here until the OSGi jars stop using compilation flags that are not compatible with Java7.
			//We MIGHT get duplicates here, since it's possible a service was added in-between the addServiceListener() and the getServiceReferences() calls.
			for( ServiceReference/* <E> */ref : Objects.firstNonNull(
					context.getServiceReferences( type.getName(), filter ), new ServiceReference[0] ) )
			{
				E service = type.cast( context.getService( ref ) );
				if( !list.contains( service ) )
				{
					list.add( service );
				}
			}
		}

		@Override
		public void serviceChanged( ServiceEvent event )
		{
			if( event.getType() == ServiceEvent.REGISTERED )
			{
				Object service = context.getService( event.getServiceReference() );
				if( type.isInstance( service ) )
				{
					list.add( type.cast( service ) );
				}
			}
			else if( event.getType() == ServiceEvent.UNREGISTERING )
			{
				list.remove( context.getService( event.getServiceReference() ) );
			}
		}

		@Override
		public void release()
		{
			context.removeServiceListener( this );
		}
	}

	private static class CollectionListData<E> implements EventHandler<CollectionEvent>, Releasable
	{
		private final ObservableList<E> list = FXCollections.observableArrayList();
		private final Class<E> type;
		private final EventFirer eventFirer;
		private final String collectionName;

		private CollectionListData( EventFirer eventFirer, String collectionName, Class<E> type,
				Iterable<? extends E> initialValues )
		{
			this.eventFirer = eventFirer;
			this.type = type;
			this.collectionName = collectionName;

			eventFirer.addEventListener( CollectionEvent.class, this );
			list.addAll( Lists.newArrayList( initialValues ) );
		}

		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( collectionName.equals( event.getKey() ) )
			{
				if( event.getEvent() == CollectionEvent.Event.ADDED )
				{
					Object element = event.getElement();
					if( type.isInstance( element ) )
					{
						list.add( type.cast( event.getElement() ) );
					}
				}
				else
				{
					list.remove( event.getElement() );
				}
			}
		}

		@Override
		public void release()
		{
			eventFirer.removeEventListener( CollectionEvent.class, this );
		}
	}

	private static class TransformedListData<F, T> implements ListChangeListener<F>, Releasable
	{
		private final ObservableList<T> list = FXCollections.observableArrayList();
		private final ObservableList<F> originalList;
		private final Function<F, T> function;
		private final LoadingCache<F, T> cache;

		private TransformedListData( ObservableList<F> originalList, final Function<F, T> function )
		{
			this.originalList = originalList;

			this.cache = CacheBuilder.newBuilder().build( new CacheLoader<F, T>()
			{
				@Override
				public T load( F key ) throws Exception
				{
					return function.apply( key );
				}
			} );

			this.function = new Function<F, T>()
			{
				@Override
				public T apply( F input )
				{
					return cache.getUnchecked( input );
				}
			};

			originalList.addListener( this );
			list.addAll( Lists.transform( originalList, this.function ) );
		}

		@Override
		public void onChanged( ListChangeListener.Change<? extends F> change )
		{
			while( change.next() )
			{
				list.removeAll( Lists.transform( change.getRemoved(), function ) );
				cache.invalidateAll( change.getRemoved() );
				list.addAll( Lists.transform( change.getAddedSubList(), function ) );
			}
		}

		@Override
		public void release()
		{
			originalList.removeListener( this );
		}
	}

	private static class FXObservableListData<E> implements ListChangeListener<E>, Releasable
	{
		private final ObservableList<E> list = FXCollections.observableArrayList();
		private final ObservableList<E> originalList;

		private FXObservableListData( final ObservableList<E> originalList )
		{
			this.originalList = originalList;
			originalList.addListener( this );
			if( Platform.isFxApplicationThread() )
			{
				list.addAll( originalList );
			}
			else
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						list.addAll( originalList );
					}
				} );
			}
		}

		@Override
		public void onChanged( final ListChangeListener.Change<? extends E> change )
		{
			if( Platform.isFxApplicationThread() )
			{
				handleChange( change );
			}
			else
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						handleChange( change );
					}
				} );
			}
		}

		@Override
		public void release()
		{
			originalList.removeListener( this );
		}

		private void handleChange( final ListChangeListener.Change<? extends E> change )
		{
			while( change.next() )
			{
				if( change.wasAdded() )
				{
					list.addAll( change.getAddedSubList() );
				}
				if( change.wasRemoved() )
				{
					list.removeAll( change.getRemoved() );
				}
			}
		}
	}

	private static class FilteredListData<E> implements ListChangeListener<E>, Releasable
	{
		private final ObservableList<E> list = FXCollections.observableArrayList();
		private final ObservableList<E> originalList;
		private final Predicate<E> filter;

		private FilteredListData( ObservableList<E> originalList, Predicate<E> condition )
		{
			this.originalList = originalList;
			this.filter = condition;

			originalList.addListener( this );
			list.addAll( Lists.newArrayList( Iterables.filter( originalList, condition ) ) );
		}

		@Override
		public void release()
		{
			originalList.removeListener( this );
		}

		@Override
		public void onChanged( ListChangeListener.Change<? extends E> change )
		{
			while( change.next() )
			{
				list.addAll( Lists.newArrayList( Iterables.filter( change.getAddedSubList(), filter ) ) );
				list.removeAll( change.getRemoved() );
			}
		}
	}

	private static class ExpressionListData<E> implements InvalidationListener, Releasable
	{
		private final ObservableList<E> list = FXCollections.observableArrayList();
		private final Callable<Iterable<E>> expression;
		private final Observable[] observables;

		public ExpressionListData( Callable<Iterable<E>> expression, Observable... observables )
		{
			this.expression = expression;
			this.observables = observables;

			for( Observable observable : observables )
			{
				observable.addListener( this );
			}
		}

		@Override
		public void release()
		{
			for( Observable observable : observables )
			{
				observable.removeListener( this );
			}
		}

		@Override
		public void invalidated( Observable arg0 )
		{
			try
			{
				list.setAll( Lists.newArrayList( expression.call() ) );
			}
			catch( Exception e )
			{
				list.clear();
			}
		}
	}
}
