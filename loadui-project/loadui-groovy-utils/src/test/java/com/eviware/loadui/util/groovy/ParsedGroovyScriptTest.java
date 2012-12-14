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
package com.eviware.loadui.util.groovy;

import java.util.Set;

import org.junit.*;

import com.google.common.collect.ImmutableSet;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ParsedGroovyScriptTest
{
	private final static String CRLF = System.getProperty( "line.separator" );
	private final static String scriptText = "/*" + CRLF + //
			" * This is a demo script, used for testing the header parsing functionality." + CRLF + //
			" * This is the second line of the description." + CRLF + //
			" *" + CRLF + //
			" * @name TestScript" + CRLF + //
			" * @id com.eviware.loadui.groovy.demo" + CRLF + //
			" *" + CRLF + //
			" * @m2repo http://repository.example.com/" + CRLF + //
			" * @dependency myGroup:myArtifact:myVersion" + CRLF + //
			" * @dependency myGroup:myOtherArtifact:myOtherVersion" + CRLF + //
			" */" + CRLF + //
			" " + CRLF + //
			"import java.lang.Object" + CRLF + //
			"" + CRLF + //
			"def object = new Object()";

	private ParsedGroovyScript script;

	@Before
	public void setup()
	{
		script = new ParsedGroovyScript( scriptText );
	}

	@Test
	public void shouldParseDescription()
	{
		String description = script.getDescription();

		assertThat( description, is( "This is a demo script, used for testing the header parsing functionality." + CRLF
				+ "This is the second line of the description." ) );
	}

	@Test
	public void shouldParseHeaders()
	{
		assertThat( script.getHeader( "name", null ), is( "TestScript" ) );
		assertThat( script.getHeaders( "name" ).size(), is( 1 ) );

		assertThat( script.getHeader( "id", null ), is( "com.eviware.loadui.groovy.demo" ) );
		assertThat( script.getHeaders( "id" ).size(), is( 1 ) );
	}

	@Test
	public void shouldHandleMultipleValuedHeaders()
	{
		assertThat( script.getHeaders( "dependency" ), is( ( Set<String> )ImmutableSet.of(
				"myGroup:myArtifact:myVersion", "myGroup:myOtherArtifact:myOtherVersion" ) ) );

		assertThat( script.getHeader( "dependency", null ), is( "myGroup:myArtifact:myVersion" ) );
	}

	@Test
	public void shouldReturnEverythingAsBody()
	{
		assertThat( script.getBody(), is( scriptText ) );
	}
}
