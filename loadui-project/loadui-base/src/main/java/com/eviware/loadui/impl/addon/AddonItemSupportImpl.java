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
package com.eviware.loadui.impl.addon;

import java.util.Collection;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonHolder.Support;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.PropertyMap;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.config.AddonItemConfig;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.impl.property.AttributeHolderSupport;
import com.eviware.loadui.impl.property.PropertyMapImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.base.Objects;

public class AddonItemSupportImpl implements AddonItem.Support, Releasable
{
	private final AddonItemHolderSupport parent;
	private final AddonItemConfig config;
	private final AddonListConfig parentListConfig;
	private final AddonItemHolderSupport addonItemHolderSupport;
	private final AddressableRegistry addressableRegistry;

	private final EventSupport eventSupport = new EventSupport();
	private final AttributeHolderSupport attributeSupport;

	private String id;
	private AddonItem owner;
	private PropertyMapImpl propertyMap;
	private AddonHolderSupportImpl addonHolderSupport;

	public AddonItemSupportImpl( AddonItemHolderSupport parent, AddonItemConfig config, AddonListConfig listConfig )
	{
		this.parent = parent;
		this.config = config;
		this.parentListConfig = listConfig;

		if( config.getAddons() == null )
			config.addNewAddons();
		addonItemHolderSupport = new AddonItemHolderSupport( config.getAddons() );

		if( !config.isSetId() )
			config.setId( BeanInjector.getBean( AddressableRegistry.class ).generateId() );
		id = config.getId();

		attributeSupport = new AttributeHolderSupport( config.getAttributes() == null ? config.addNewAttributes()
				: config.getAttributes() );

		addressableRegistry = BeanInjector.getBean( AddressableRegistry.class );
	}

	@Override
	public void init( AddonItem owner )
	{
		this.owner = owner;

		try
		{
			addressableRegistry.register( owner );
		}
		catch( DuplicateAddressException e )
		{
			config.setId( addressableRegistry.generateId() );
			id = config.getId();
			try
			{
				addressableRegistry.register( owner );
			}
			catch( DuplicateAddressException e1 )
			{
				//This shouldn't happen.
				throw new RuntimeException( e1 );
			}
		}
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public PropertyMap getPropertyMap( PropertyHolder owner )
	{
		if( propertyMap == null )
		{
			propertyMap = new PropertyMapImpl( owner, BeanInjector.getBean( ConversionService.class ),
					config.getProperties() == null ? config.addNewProperties() : config.getProperties() );
		}

		return propertyMap;
	}

	@Override
	public Support getAddonHolderSupport( AddonHolder owner )
	{
		if( addonHolderSupport == null )
		{
			addonHolderSupport = new AddonHolderSupportImpl( owner, config.getAddons() == null ? config.addNewAddons()
					: config.getAddons() );
		}

		return addonHolderSupport;
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributeSupport.getAttribute( key, defaultValue );
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributeSupport.setAttribute( key, value );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributeSupport.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributeSupport.getAttributes();
	}

	@Override
	public String getType()
	{
		return config.getType();
	}

	@Override
	public void delete()
	{
		for( int i = parentListConfig.sizeOfAddonArray() - 1; i <= 0; i-- )
		{
			if( Objects.equal( getId(), parentListConfig.getAddonArray( i ).getId() ) )
			{
				parent.removeAddonItem( this );
				parentListConfig.removeAddon( i );
				return;
			}
		}
	}

	@Override
	public AddonItem.Support createAddonItemSupport( String type )
	{
		return addonItemHolderSupport.createAddonItemSupport( type );
	}

	@Override
	public Collection<AddonItem.Support> getAddonItemSupports( String type )
	{
		return addonItemHolderSupport.getAddonItemSupports( type );
	}

	@Override
	public void release()
	{
		addressableRegistry.unregister( owner );
		eventSupport.clearEventListeners();
	}
}