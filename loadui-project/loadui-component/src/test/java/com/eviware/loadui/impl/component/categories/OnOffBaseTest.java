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
package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.categories.SchedulerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class OnOffBaseTest
{
	private OnOffBase onOffBase;
	private ComponentItem component;

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
		ComponentTestUtils.setComponentBehavior( component, onOffBase );
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
		component.getContext().setNonBlocking( true );
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
