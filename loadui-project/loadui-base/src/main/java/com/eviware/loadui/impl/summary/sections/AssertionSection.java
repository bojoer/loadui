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
package com.eviware.loadui.impl.summary.sections;

import java.util.Collection;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.AssertionMetricsTableModel;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class AssertionSection extends MutableSectionImpl
{
	private final Function<CanvasItem, Iterable<? extends AssertionItem<?>>> getAssertions = new Function<CanvasItem, Iterable<? extends AssertionItem<?>>>()
	{
		@Override
		public Collection<? extends AssertionItem<?>> apply( CanvasItem child )
		{
			return child.getAddon( AssertionAddon.class ).getAssertions();
		}
	};

	public AssertionSection( CanvasItem canvas )
	{
		super( "Assertion" );

		Iterable<AssertionItem<?>> childAssertions = Iterables.concat( Iterables.transform( canvas.getChildren(),
				getAssertions ) );

		Iterable<AssertionItem<?>> allAssertions = Iterables.concat( childAssertions,
				canvas.getAddon( AssertionAddon.class ).getAssertions() );

		addTable( "Assertions", new AssertionMetricsTableModel( allAssertions ) );
	}
}
