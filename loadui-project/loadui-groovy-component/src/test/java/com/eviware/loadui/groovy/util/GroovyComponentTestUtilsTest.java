package com.eviware.loadui.groovy.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.util.component.ComponentTestUtils;

public class GroovyComponentTestUtilsTest
{
	@Test
	public void shouldCreateComponent() throws ComponentCreationException, IOException
	{
		File scriptDir = new File( "target", "scripts" );
		scriptDir.mkdirs();

		File scriptFile = new File( scriptDir, "Test.groovy" );
		scriptFile.createNewFile();

		GroovyComponentTestUtils.initialize( scriptDir.getPath() );
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		ComponentItem component = GroovyComponentTestUtils.createComponent( "Test" );

		assertThat( component.getBehavior().getClass().getSimpleName(), startsWith( "Groovy" ) );
	}
}
