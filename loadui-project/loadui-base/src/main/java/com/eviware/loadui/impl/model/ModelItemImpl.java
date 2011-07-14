/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.model;

import java.util.Collection;
import java.util.EventObject;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertyMap;
import com.eviware.loadui.config.ModelItemConfig;
import com.eviware.loadui.impl.addon.AddonHolderSupportImpl;
import com.eviware.loadui.impl.property.AttributeHolderSupport;
import com.eviware.loadui.impl.property.PropertyMapImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.collect.ImmutableList;

public abstract class ModelItemImpl<Config extends ModelItemConfig> implements ModelItem
{
	public static final Logger log = LoggerFactory.getLogger( ModelItemImpl.class );

	public static final String helpUrl = "http://www.loadui.org";

	private final Config config;
	private final EventSupport eventSupport = new EventSupport();
	private final AttributeHolderSupport attributeHolderSupport;
	private final PropertyMap properties;
	private String id;
	private String label;
	private final Property<String> description;
	private boolean released = false;
	private boolean initialized = false;
	protected final AddressableRegistry addressableRegistry;
	private final AddonHolderSupportImpl addonSupport;

	public ModelItemImpl( @Nonnull Config config )
	{
		this.config = config;

		addressableRegistry = BeanInjector.getBean( AddressableRegistry.class );
		if( !config.isSetId() )
			config.setId( addressableRegistry.generateId() );

		id = config.getId();

		label = config.getLabel();

		properties = new PropertyMapImpl( this, BeanInjector.getBean( ConversionService.class ),
				config.getProperties() == null ? config.addNewProperties() : config.getProperties() );

		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() == null ? config.addNewAttributes()
				: config.getAttributes() );
		addonSupport = new AddonHolderSupportImpl( this, config.getAddons() == null ? config.addNewAddons()
				: config.getAddons() );

		description = createProperty( DESCRIPTION_PROPERTY, String.class, "" );
	}

	@OverridingMethodsMustInvokeSuper
	public void init()
	{
		if( initialized )
			throw new RuntimeException( "init() called on already initialized object: " + this );

		try
		{
			addressableRegistry.register( this );
		}
		catch( DuplicateAddressException e )
		{
			id = addressableRegistry.generateId();
			config.setId( id );
			try
			{
				addressableRegistry.register( this );
			}
			catch( DuplicateAddressException e1 )
			{
				e1.printStackTrace();
			}
		}

		initialized = true;
	}

	public boolean isReleased()
	{
		return released;
	}

	public Config getConfig()
	{
		return config;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel( String label )
	{
		if( label != null && !label.equals( getLabel() ) )
		{
			config.setLabel( label );
			this.label = label;
			fireBaseEvent( LABEL );
		}
	}

	public void setId( String id ) throws DuplicateAddressException
	{
		String oldId = config.getId();
		addressableRegistry.unregister( this );
		config.setId( id );
		try
		{
			addressableRegistry.register( this );
		}
		catch( DuplicateAddressException e )
		{
			config.setId( oldId );
			addressableRegistry.register( this );
			throw e;
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void release()
	{
		if( !released )
		{
			fireBaseEvent( RELEASED );
			released = true;
			addressableRegistry.unregister( this );
			eventSupport.clearEventListeners();
			ReleasableUtils.releaseAll( addonSupport, attributeHolderSupport );
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void delete()
	{
		log.debug( "Deleting {}", this );
		if( !released )
		{
			fireBaseEvent( RELEASED );
			fireBaseEvent( DELETED );
			released = true;
			release();
			addressableRegistry.unregister( this );
			eventSupport.clearEventListeners();
		}
		else
			throw new RuntimeException( "Cannot delete released ModelItem" );
	}

	@Override
	public <T extends Addon> T getAddon( Class<T> type )
	{
		return addonSupport.getAddon( type );
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
	public Collection<Property<?>> getProperties()
	{
		return ImmutableList.copyOf( properties.values() );
	}

	@Override
	public Property<?> getProperty( String propertyName )
	{
		return properties.get( propertyName );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType )
	{
		return properties.createProperty( propertyName, propertyType );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue )
	{
		return properties.createProperty( propertyName, propertyType, initialValue );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			boolean propagates )
	{
		return properties.createProperty( propertyName, propertyType, initialValue, propagates );
	}

	@Override
	public void renameProperty( String oldName, String newName )
	{
		properties.renameProperty( oldName, newName );
	}

	@Override
	public void deleteProperty( String propertyName )
	{
		properties.remove( propertyName );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		if( !released )
			eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void triggerAction( String actionName )
	{
		log.debug( "Triggering action '{}' on ModelItem '{}'", actionName, this );
		fireEvent( new ActionEvent( this, actionName ) );
	}

	@Override
	public void fireEvent( EventObject event )
	{
		if( !released )
			eventSupport.fireEvent( event );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	public void fireBaseEvent( String eventKey )
	{
		fireEvent( new BaseEvent( this, eventKey ) );
	}

	public void fireCollectionEvent( String collection, CollectionEvent.Event event, Object element )
	{
		fireEvent( new CollectionEvent( this, collection, event, element ) );
	}

	public void firePropertyEvent( Property<?> property, PropertyEvent.Event event, Object argument )
	{
		fireEvent( new PropertyEvent( this, property, event, argument ) );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[label=" + getLabel() + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		return prime * result + getId().hashCode();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		ModelItemImpl<?> other = ( ModelItemImpl<?> )obj;
		return getId().equals( other.getId() );
	}

	@Override
	public String getHelpUrl()
	{
		return helpUrl;
	}

	@Override
	public String getDescription()
	{
		return description.getValue();
	}

	@Override
	public void setDescription( String description )
	{
		this.description.setValue( description );
	}
}
