package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.util.component.ComponentTestUtils;

public class BaseCategoryTest
{
	private ComponentItemImpl component;
	private BaseCategory baseCategory;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		baseCategory = new BaseCategory( component.getContext() )
		{
			@Override
			public String getColor()
			{
				return null;
			}

			@Override
			public String getCategory()
			{
				return null;
			}
		};
	}

	@Test
	public void shouldCreateTotals() throws Exception
	{
		@SuppressWarnings( "unchecked" )
		Callable<Number> callable = mock( Callable.class );
		when( callable.call() ).thenReturn( 7 );

		Value<Number> total = baseCategory.createTotal( "total", callable );
		assertThat( total.getValue().intValue(), is( 7 ) );

		when( callable.call() ).thenReturn( 11 );
		assertThat( total.getValue().intValue(), is( 11 ) );

		baseCategory.removeTotal( "total" );
		assertThat( total.getValue().intValue(), is( 0 ) );
	}
}
