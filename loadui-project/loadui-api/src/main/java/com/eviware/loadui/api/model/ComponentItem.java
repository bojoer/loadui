/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.api.model;

import java.util.Collection;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.summary.MutableChapter;

/**
 * A loadUI Component.
 * 
 * @author dain.nilsson
 */
public interface ComponentItem extends CanvasObjectItem, CounterHolder
{
	public static final String CATEGORY = ComponentItem.class.getName() + "@category";

	public static final String INVALID = ComponentItem.class.getName() + "@invalid";

	public static final String TYPE = ComponentItem.class.getName() + "@type";
	
	public static final String BUSY = ComponentItem.class.getName() + "@busy";

	/**
	 * Action which can be triggered on a busy component to indicate that the
	 * current action should be aborted so that the Component may become non-busy
	 * as soon as possible.
	 */
	public static final String CANCEL_ACTION = "CANCEL";

	/**
	 * Gets the type of the component. This corresponds to the label of the
	 * ComponentDescriptor used when creating a component.
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Gets the behavior of the ComponentItem.
	 * 
	 * @return The ComponentBehavior for the component.
	 */
	public ComponentBehavior getBehavior();

	/**
	 * Get the context object for this component.
	 * 
	 * @return The ComponentContext for the component.
	 */
	public ComponentContext getContext();

	/**
	 * Get the component category for this component.
	 * 
	 * @return The category, as a String.
	 */
	public String getCategory();

	/**
	 * Get the layout for displaying the component.
	 * 
	 * @return The LayoutComponent describing the layout for this CompontentItem.
	 */
	public LayoutComponent getLayout();

	/**
	 * Get the layout for displaying the component in compact mode.
	 * 
	 * @return The LayoutComponent describing the layout for this CompontentItem
	 *         in compact mode.
	 */
	public LayoutComponent getCompactLayout();

	/**
	 * Get the SettingsLayoutContainers which should be displayed as tabs in the
	 * Settings Panel for the ComponentItem.
	 * 
	 * @return A Collection of the SettingsLayoutContainers to display.
	 */
	public Collection<SettingsLayoutContainer> getSettingsTabs();

	/**
	 * Checks to see if the ComponentItem is in an invalid state and won't
	 * function properly. This may be due to an invalid configuration or a
	 * missing resource, etc.
	 * 
	 * @return
	 */
	public boolean isInvalid();

	/**
	 * Set the invalid state of the ComponentItem.
	 * 
	 * @param state
	 */
	public void setInvalid( boolean state );
	
	/**
	 * Checks to see if the ComponentItem is in a busy (working) state.
	 * Once a COMPLETE or STOP action is triggered, a ComponentItem should move into a non-busy state as soon as possible.
	 * A ComponentItem doesn't need to ever be in a busy state.  
	 * @return
	 */
	public boolean isBusy();
	
	/**
	 * Sets the busy state of the ComponentItem.
	 * 
	 * @param state
	 */
	public void setBusy( boolean state );
	
	/**
	 * Called on a ComponentItem to generate a summary of its run.
	 * 
	 * @param summary
	 */
	public void generateSummary( MutableChapter summary );
}
