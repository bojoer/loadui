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
package com.eviware.loadui.util.groovy;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;

/**
 * Parses the header of a Groovy script and provides methods for getting its
 * data.
 * 
 * @author dain.nilsson
 */
@Immutable
public final class ParsedGroovyScript
{
	public static final String DESCRIPTION = "description";
	private static final String CRLF = System.getProperty( "line.separator" );
	private static final Pattern headerPattern = Pattern.compile( "(?s)\\s*/\\*+\\s?(.*?)\\*/" );

	private final String script;
	private final ImmutableSetMultimap<String, String> headers;
	private final String description;

	public ParsedGroovyScript( @Nonnull String script )
	{
		this.script = script;

		Matcher m = headerPattern.matcher( script );
		ImmutableSetMultimap.Builder<String, String> mapBuilder = ImmutableSetMultimap.builder();
		StringBuilder descriptionBuilder = new StringBuilder();

		if( m.find() )
		{
			for( String line : m.group( 1 ).split( "\r\n|\r|\n" ) )
			{
				if( line.startsWith( " *" ) )
					line = line.substring( 2 );
				line = line.trim();
				if( line.startsWith( "@" ) )
				{
					String[] parts = line.split( "\\s", 2 );
					mapBuilder.put( parts[0].substring( 1 ), parts[1].trim() );
				}
				else
					descriptionBuilder.append( line ).append( CRLF );
			}
		}

		headers = mapBuilder.build();
		this.description = descriptionBuilder.toString().trim();
	}

	/**
	 * Returns the full body (including the header) of the script.
	 * 
	 * @return
	 */
	public String getBody()
	{
		return script;
	}

	/**
	 * Returns the description of the Groovy script.
	 * 
	 * @return
	 */
	@Nonnull
	public String getDescription()
	{
		return description;
	}

	/**
	 * Returns the value of the given header (or the provided default value if
	 * the header does not exist). If the header has multiple values, the first
	 * of these will be returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@Nullable
	public String getHeader( @Nonnull String key, @Nullable String defaultValue )
	{
		return Iterables.getFirst( headers.get( key ), defaultValue );
	}

	/**
	 * Returns all values for the given header. If the header doesn't exist, an
	 * empty Set is returned.
	 * 
	 * @param key
	 * @return
	 */
	@Nonnull
	public Set<String> getHeaders( @Nonnull String key )
	{
		return headers.get( key );
	}
}
