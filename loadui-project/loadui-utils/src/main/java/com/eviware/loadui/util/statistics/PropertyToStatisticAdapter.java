/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.statistics;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticVariable;

/**
 * Adapts events fired for a Property to update a StatisticVariable.Mutable.
 * 
 * @author dain.nilsson
 * @param <T>
 */
public class PropertyToStatisticAdapter<T extends Number>
{
	private final Property<T> property;
	private final StatisticVariable.Mutable statisticVariable;

	public PropertyToStatisticAdapter( Property<T> property, StatisticVariable.Mutable statisticVariable )
	{
		this.property = property;
		this.statisticVariable = statisticVariable;

		property.getOwner().addEventListener( PropertyEvent.class, new PropertyListener() );
	}

	private class PropertyListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( PropertyEvent.Event.VALUE == event.getEvent() && event.getProperty() == property )
				statisticVariable.update( System.currentTimeMillis(), property.getValue() );
		}
	}
}
