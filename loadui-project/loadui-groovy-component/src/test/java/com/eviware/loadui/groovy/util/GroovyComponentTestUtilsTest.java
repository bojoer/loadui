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

	private ComponentItem createSimpleScriptComponent() throws IOException, ComponentCreationException
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
