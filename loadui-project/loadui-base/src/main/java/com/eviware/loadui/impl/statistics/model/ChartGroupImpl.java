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
package com.eviware.loadui.impl.statistics.model;

import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProvider;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProviderFactory;
import com.eviware.loadui.config.ChartConfig;
import com.eviware.loadui.config.ChartGroupConfig;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.impl.model.OrderedCollectionSupport;
import com.eviware.loadui.impl.property.AttributeHolderSupport;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.events.EventSupport;

public class ChartGroupImpl implements ChartGroup
{
	public final static Logger log = LoggerFactory.getLogger( ChartGroupImpl.class );

	private final StatisticsManager statisticsManager;
	private final StatisticPageImpl parent;
	private final OrderedCollectionSupport<Chart> collectionSupport;
	private final EventSupport eventSupport = new EventSupport( this );
	private final ChartViewProviderFactory providerFactory;
	private final TemplateListener templateListener = new TemplateListener();

	private ChartGroupConfig config;
	private AttributeHolderSupport attributeHolderSupport;
	private ChartViewProvider<?> provider;
	private StatisticTemplateGroovyScript template;

	public ChartGroupImpl( StatisticPageImpl parent, ChartGroupConfig config )
	{
		this.parent = parent;
		this.config = config;

		collectionSupport = new OrderedCollectionSupport<>( this );
		if( config.getAttributes() == null )
			config.addNewAttributes();
		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() );

		providerFactory = BeanInjector.getBean( ChartViewProviderFactory.class );
		statisticsManager = BeanInjector.getBean( StatisticsManager.class );
		statisticsManager.addEventListener( BaseEvent.class, templateListener );

		provider = providerFactory.buildProvider( getType(), this );

		for( ChartConfig chartConfig : config.getChartList() )
		{
			try
			{
				collectionSupport.addChild( new ChartImpl( this, chartConfig ) );
			}
			catch( IllegalArgumentException e )
			{
				log.error( "Unable to add Chart to ChartGroup:", e );
			}
		}

		refreshScript();
	}

	@Override
	public final String getType()
	{
		return config.getType();
	}

	@Override
	public void setType( String type )
	{
		if( !getType().equals( type ) )
		{
			config.setType( type );

			ReleasableUtils.release( provider );
			provider = providerFactory.buildProvider( type, this );

			fireEvent( new BaseEvent( this, TYPE ) );
		}
	}

	@Override
	public String getLabel()
	{
		return config.isSetTitle() ? config.getTitle() : "";
	}

	@Override
	public void setLabel( String title )
	{
		if( !getLabel().equals( title ) )
		{
			config.setTitle( title );
			fireEvent( new BaseEvent( this, LABEL ) );
		}
	}

	@Override
	public final String getTemplateScript()
	{
		return config.getTemplateScript() == null ? "" : config.getTemplateScript();
	}

	@Override
	public void setTemplateScript( String templateScript )
	{
		if( !getTemplateScript().equals( templateScript ) )
		{
			config.setTemplateScript( templateScript );
			refreshScript();
			fireEvent( new BaseEvent( this, TEMPLATE_SCRIPT ) );
		}
	}

	private void refreshScript()
	{
		ReleasableUtils.release( template );

		if( !StringUtils.isNullOrEmpty( getTemplateScript() ) )
		{
			template = new StatisticTemplateGroovyScript( getTemplateScript() );
			for( StatisticHolder holder : statisticsManager.getStatisticHolders() )
				template.filter( holder, this );
		}
		else
		{
			template = null;
		}
	}

	@Override
	public ChartView getChartView()
	{
		return provider.getChartViewForChartGroup();
	}

	@Override
	public ChartView getChartViewForChart( Chart chart )
	{
		return provider.getChartViewForChart( chart );
	}

	@Override
	public ChartView getChartViewForSource( String source )
	{
		return provider.getChartViewForSource( source );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public Collection<ChartView> getChartViewsForCharts()
	{
		return ( Collection<ChartView> )provider.getChartViewsForCharts();
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public Collection<ChartView> getChartViewsForSources()
	{
		return ( Collection<ChartView> )provider.getChartViewsForSources();
	}

	@Override
	public Chart createChart( Owner statisticHolder )
	{
		for( Chart chart : getChildren() )
			if( chart.getOwner() == statisticHolder )
				return chart;

		ChartConfig chartConfig = config.addNewChart();
		chartConfig.setStatisticHolder( statisticHolder.getId() );
		ChartImpl chart = new ChartImpl( this, chartConfig );
		collectionSupport.addChild( chart );
		return chart;
	}

	@Override
	public void moveChart( Chart chart, int index )
	{
		ChartConfig[] chartArray = XmlBeansUtils.moveArrayElement(
				config.getChartList().toArray( new ChartConfig[config.sizeOfChartArray()] ), indexOf( chart ), index );
		config.setChartArray( chartArray );
		collectionSupport.moveChild( chart, index );
		for( int i = 0; i < chartArray.length; i++ )
			( ( ChartImpl )collectionSupport.getChildAt( i ) ).setConfig( config.getChartArray( i ) );
	}

	@Override
	public Set<String> getSources()
	{
		Set<String> sources = new HashSet<>();
		for( Chart chart : getChildren() )
			sources.addAll( ( ( ChartImpl )chart ).getSources() );

		sources.remove( StatisticVariable.MAIN_SOURCE );

		return sources;
	}

	@Override
	public void delete()
	{
		parent.removeChild( this );
		release();
	}

	void removeChild( Chart child )
	{
		int index = collectionSupport.indexOf( child );
		if( index >= 0 )
		{
			config.removeChart( index );
			collectionSupport.removeChild( child );
		}
	}

	@Override
	public Collection<Chart> getChildren()
	{
		return collectionSupport.getChildren();
	}

	@Override
	public int getChildCount()
	{
		return collectionSupport.getChildCount();
	}

	@Override
	public int indexOf( Chart child )
	{
		return collectionSupport.indexOf( child );
	}

	@Override
	public Chart getChildAt( int index )
	{
		return collectionSupport.getChildAt( index );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributeHolderSupport.getAttribute( key, defaultValue );
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributeHolderSupport.setAttribute( key, value );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributeHolderSupport.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributeHolderSupport.getAttributes();
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	void setConfig( ChartGroupConfig chartGroupConfig )
	{
		config = chartGroupConfig;
		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() );
		for( int i = 0; i < getChildCount(); i++ )
			( ( ChartImpl )getChildAt( i ) ).setConfig( config.getChartArray( i ) );
	}

	@Override
	public void release()
	{
		statisticsManager.removeEventListener( BaseEvent.class, templateListener );
		fireEvent( new BaseEvent( this, RELEASED ) );
		ReleasableUtils.releaseAll( template, provider, attributeHolderSupport, collectionSupport, eventSupport );
	}

	private class TemplateListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof CollectionEvent )
			{
				CollectionEvent cEvent = ( CollectionEvent )event;
				if( CollectionEvent.Event.ADDED == cEvent.getEvent() )
				{
					if( template != null )
						template.filter( ( StatisticHolder )cEvent.getElement(), ChartGroupImpl.this );
				}
			}
			else if( StatisticsManager.STATISTIC_HOLDER_UPDATED.equals( event.getKey() ) )
			{
				if( template != null )
					template.filter( ( StatisticHolder )event.getSource(), ChartGroupImpl.this );
			}
		}
	}
}