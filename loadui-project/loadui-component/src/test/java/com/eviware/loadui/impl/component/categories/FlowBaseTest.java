package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class FlowBaseTest
{
	private FlowBase flowBase;
	private ComponentItem component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		flowBase = new FlowBase( component.getContext() )
		{
		};
		ComponentTestUtils.setComponentBehavior( component, flowBase );
	}

	@Test
	public void shouldCreateOutgoingTerminals()
	{
		assertThat( flowBase.getOutgoingTerminalList().isEmpty(), is( true ) );

		OutputTerminal output1 = flowBase.createOutgoing();
		OutputTerminal output2 = flowBase.createOutgoing();
		OutputTerminal output3 = flowBase.createOutgoing();

		assertThat( flowBase.getOutgoingTerminalList().size(), is( 3 ) );

		assertThat( flowBase.deleteOutgoing(), sameInstance( output3 ) );

		OutputTerminal output3b = flowBase.createOutgoing();

		assertThat( flowBase.deleteOutgoing(), sameInstance( output3b ) );
		assertThat( flowBase.deleteOutgoing(), sameInstance( output2 ) );
		assertThat( flowBase.deleteOutgoing(), sameInstance( output1 ) );

		assertThat( flowBase.getOutgoingTerminalList().isEmpty(), is( true ) );
	}

	@Test
	public void shouldDelegateLikeFunction()
	{
		OutputTerminal dummyOut = mock( OutputTerminal.class );
		InputTerminal dummyIn = mock( InputTerminal.class );
		when( dummyIn.likes( dummyOut ) ).thenReturn( true );

		InputTerminal input = flowBase.getIncomingTerminal();
		OutputTerminal output1 = flowBase.createOutgoing();

		assertThat( input.likes( dummyOut ), is( false ) );

		Connection connection1 = output1.connectTo( dummyIn );
		assertThat( input.likes( dummyOut ), is( true ) );

		OutputTerminal output2 = flowBase.createOutgoing();
		output2.connectTo( dummyIn );
		assertThat( input.likes( dummyOut ), is( true ) );

		connection1.disconnect();
		assertThat( input.likes( dummyOut ), is( true ) );

		flowBase.deleteOutgoing();
		assertThat( input.likes( dummyOut ), is( false ) );
	}

	@Test
	public void shouldPropagateSignature()
	{
		Map<String, Class<? extends Object>> signature = ImmutableMap.of( "A", Object.class, "B", String.class );

		ComponentContext context = ComponentTestUtils.createComponentItem().getContext();
		OutputTerminal otherOutput = context.createOutput( "output" );
		context.setSignature( otherOutput, signature );

		InputTerminal input = flowBase.getIncomingTerminal();
		OutputTerminal output1 = flowBase.createOutgoing();

		//Ensure that the handler handling the connection gets invoked in the event thread.
		component.getContext().setNonBlocking( true );

		Connection connection = otherOutput.connectTo( input );

		assertThat( output1.getMessageSignature(), equalTo( signature ) );

		OutputTerminal output2 = flowBase.createOutgoing();

		assertThat( output2.getMessageSignature(), equalTo( signature ) );

		connection.disconnect();
		assertThat( output1.getMessageSignature().isEmpty(), is( true ) );
		assertThat( output2.getMessageSignature().isEmpty(), is( true ) );
	}
}
