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
package com.eviware.loadui.util.assertion;

import java.util.LinkedList;

import com.eviware.loadui.api.assertion.Constraint;
import com.google.common.collect.Lists;

/**
 * Wraps an existing Constraint adding a tolerance for it. The tolerance is
 * defined as a number of allowed occurrences within a given period. Unless the
 * original Constraint fails more than occurrences times within the period, the
 * ToleratedConstraint will not
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public class ToleratedConstraint<T> implements Constraint<T>
{
	private static final long serialVersionUID = 5606018394038610766L;

	private final Constraint<T> constraint;
	private final long period;
	private final int occurrences;

	private final LinkedList<Long> failures = Lists.newLinkedList();

	public ToleratedConstraint( Constraint<T> constraint, long period, int occurrences )
	{
		this.constraint = constraint;
		this.period = period;
		this.occurrences = occurrences;
	}

	@Override
	public boolean validate( T value )
	{
		if( !constraint.validate( value ) )
		{
			long timestamp = System.currentTimeMillis();

			failures.add( timestamp );
			if( failures.size() >= occurrences )
			{
				long expiry = timestamp - period;
				while( !failures.isEmpty() && failures.getFirst() < expiry )
				{
					failures.removeFirst();
				}

				if( failures.size() >= occurrences )
				{
					failures.clear();
					return false;
				}
			}
		}

		return true;
	}
}