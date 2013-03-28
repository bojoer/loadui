/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.groovy.util;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.groovy.GroovyEnvironment;

public class GroovyComponentTestUtilsTest
{
	@Test
	public void shouldCreateComponent() throws ComponentCreationException, IOException
	{
		ComponentItem component = createSimpleScriptComponent();

		assertThat( component.getBehavior().getClass().getSimpleName(), startsWith( "Groovy" ) );
	}

	private static ComponentItem createSimpleScriptComponent() throws IOException, ComponentCreationException
	{
		File scriptDir = new File( "target", "scripts" );
		scriptDir.mkdirs();

		File scriptFile = new File( scriptDir, "Test.groovy" );
		scriptFile.createNewFile();

		GroovyComponentTestUtils.initialize( scriptDir.getPath() );
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		ComponentItem component = GroovyComponentTestUtils.createComponent( "Test" );
		return component;
	}

	@Test
	public void shouldReturnGroovyEnvorinment() throws ComponentCreationException, IOException
	{
		GroovyEnvironment environment = GroovyComponentTestUtils.getEnvironment( createSimpleScriptComponent() );

		assertThat( environment, notNullValue() );
	}
}
