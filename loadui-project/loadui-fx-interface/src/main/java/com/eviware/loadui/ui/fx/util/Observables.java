package com.eviware.loadui.ui.fx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Observables
{
	public static Group group( Observable... observables )
	{
		return new Group( observables );
	}

	public static class Group extends ObservableBase
	{
		private final ObservableList<Observable> observables = FXCollections.observableArrayList();
		private final InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				fireInvalidation();
			}
		};

		private Group( Observable... observables )
		{
			this.observables.addListener( new ListChangeListener<Observable>()
			{
				@Override
				public void onChanged( ListChangeListener.Change<? extends Observable> change )
				{
					while( change.next() )
					{
						for( Observable observable : change.getAddedSubList() )
						{
							observable.addListener( invalidationListener );
						}
						for( Observable observable : change.getRemoved() )
						{
							observable.removeListener( invalidationListener );
						}
					}
				}
			} );

			for( Observable observable : observables )
			{
				this.observables.add( observable );
			}
		}

		public ObservableList<Observable> getObservables()
		{
			return observables;
		}

		@Override
		public void fireInvalidation()
		{
			super.fireInvalidation();
		}
	}
}
