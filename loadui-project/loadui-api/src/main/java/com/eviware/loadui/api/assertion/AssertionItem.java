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
package com.eviware.loadui.api.assertion;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Labeled;

/**
 * An assertion of a ListenableValue belonging to an Addressable.
 * 
 * @author dain.nilsson
 */
public interface AssertionItem<T> extends AddonItem, Chart.Owner, Labeled, Describable
{
	/**
	 * Returns the Addressable to which the asserted value belongs.
	 * 
	 * @return
	 */
	public Addressable getParent();

	/**
	 * Returns the ListenableValue which is being asserted.
	 * 
	 * @return
	 */
	public ListenableValue<T> getValue();

	/**
	 * Returns the Constraint used for asserting the value.
	 * 
	 * @return
	 */
	public Constraint<? super T> getConstraint();

	/**
	 * Returns the tolerance period in which to accept the specified number of
	 * constraint failure occurrences, before triggering an assertion failure.
	 * 
	 * @return
	 */
	public int getTolerancePeriod();

	/**
	 * Returns the number of times the constraint must fail within the tolerance
	 * period before triggering an assertion failure.
	 * 
	 * @return
	 */
	public int getToleranceAllowedOccurrences();

	/**
	 * Mutable version of AssertionItem.
	 * 
	 * @author dain.nilsson
	 */
	public interface Mutable<T> extends AssertionItem<T>, Labeled.Mutable
	{
		/**
		 * Sets the tolerance of the AssertionItem.
		 * 
		 * @param period
		 * @param allowedOccurrences
		 */
		public void setTolerance( int period, int allowedOccurrences );

		/**
		 * Sets the Constraint of the AssertionItem.
		 * 
		 * @param constraint
		 */
		public void setConstraint( Constraint<? super T> constraint );
	}
}
