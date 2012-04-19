package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.categories.SchedulerCategory;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class OnOffBaseTest
{
	private OnOffBase onOffBase;
	private ComponentItemImpl component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		onOffBase = new OnOffBase( component.getContext() )
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
		component.setBehavior( onOffBase );
	}

	@Test
	public void stateTerminalShouldLikeSchedulerMessage()
	{
		OutputTerminal schedulerOutput = mock( OutputTerminal.class );
		when( schedulerOutput.getMessageSignature() ).thenReturn( ImmutableMap.<String, Class<?>> of() );
		assertThat( onOffBase.getStateTerminal().likes( schedulerOutput ), is( false ) );

		when( schedulerOutput.getMessageSignature() ).thenReturn(
				ImmutableMap.<String, Class<?>> of( SchedulerCategory.ENABLED_MESSAGE_PARAM, Boolean.class ) );
		assertThat( onOffBase.getStateTerminal().likes( schedulerOutput ), is( true ) );
	}

	@Test
	public void shouldChangeStateWhenTriggeredByMessage()
	{
		Property<Boolean> stateProperty = onOffBase.getStateProperty();
		assertThat( stateProperty.getValue(), is( true ) );

		ComponentTestUtils.sendMessage( onOffBase.getStateTerminal(),
				ImmutableMap.of( SchedulerCategory.ENABLED_MESSAGE_PARAM, false ) );

		assertThat( stateProperty.getValue(), is( false ) );

		ComponentTestUtils.sendMessage( onOffBase.getStateTerminal(),
				ImmutableMap.of( SchedulerCategory.ENABLED_MESSAGE_PARAM, true ) );

		assertThat( stateProperty.getValue(), is( true ) );
	}
}
