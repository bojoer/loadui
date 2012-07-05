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
package com.eviware.loadui.impl.addon;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.config.AddonItemConfig;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.config.LoaduiAddonItemDocumentConfig;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class AddonItemHolderSupport implements Releasable
{
	private static final Logger log = LoggerFactory.getLogger( AddonItemHolderSupport.class );

	private final AddonListConfig listConfig;
	private final Multimap<String, AddonItemSupportImpl> addonItems = HashMultimap.create();
	private final Set<String> loadedTypes = Sets.newHashSet();

	public AddonItemHolderSupport( AddonListConfig listConfig )
	{
		this.listConfig = listConfig;
	}

	public AddonItem.Support createAddonItemSupport( @Nonnull String type )
	{
		loadType( type );

		final AddonItemConfig addonConfig = listConfig.addNewAddon();
		addonConfig.setType( type );
		final AddonItemSupportImpl addonItem = new AddonItemSupportImpl( this, addonConfig, listConfig );
		addonItems.put( type, addonItem );

		return addonItem;
	}

	public void loadType( @Nonnull String type )
	{
		if( loadedTypes.add( type ) )
		{
			for( AddonItemConfig config : listConfig.getAddonList() )
			{
				if( type.equals( config.getType() ) )
				{
					addonItems.put( type, new AddonItemSupportImpl( this, config, listConfig ) );
				}
			}
		}
	}

	public Collection<AddonItem.Support> getAddonItemSupports( @Nonnull String type )
	{
		loadType( type );

		return ImmutableSet.<AddonItem.Support> copyOf( addonItems.get( type ) );
	}

	public void removeAddonItem( @Nonnull AddonItem.Support child )
	{
		addonItems.get( child.getType() ).remove( child );
	}

	public String exportAddonItemSupport( AddonItem.Support support )
	{
		Preconditions.checkArgument( addonItems.containsEntry( support.getType(), support ),
				"AddonItem.Support does not belong to this AddonItemHolder!" );

		LoaduiAddonItemDocumentConfig doc = LoaduiAddonItemDocumentConfig.Factory.newInstance();
		doc.addNewLoaduiAddonItem().set( ( ( AddonItemSupportImpl )support ).getConfig() );

		return doc.xmlText();
	}

	public AddonItem.Support importAddonItemSupport( String exportedAddonItemSupport )
	{
		try
		{
			LoaduiAddonItemDocumentConfig doc = LoaduiAddonItemDocumentConfig.Factory.parse( exportedAddonItemSupport );

			final AddonItemConfig addonConfig = listConfig.addNewAddon();
			addonConfig.set( doc.getLoaduiAddonItem() );

			final AddonItemSupportImpl addonItem = new AddonItemSupportImpl( this, addonConfig, listConfig );
			addonItems.put( addonConfig.getType(), addonItem );

			return addonItem;
		}
		catch( XmlException e )
		{
			log.error( "Unable to parse AddonItemSupport!", e );
			return null;
		}
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( addonItems.values() );
	}
}
