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
package com.eviware.loadui.api.dispatch;

import java.util.concurrent.ExecutorService;

/**
 * Used to manage the global ExecutorService.
 * 
 * @author dain.nilsson
 */
public interface ExecutorManager
{
	/**
	 * Gets the ExecutorService.
	 * 
	 * @return
	 */
	public ExecutorService getExecutor();

	/**
	 * Gets the maximum thread pool size for the ExecutorService.
	 * 
	 * @return
	 */
	public int getMaxPoolSize();

	/**
	 * Sets the maximum thread pool size for the ExecutorService.
	 * 
	 * @param size
	 */
	public void setMaxPoolSize( int size );

	public int getMaxQueueSize();

	public void setMaxQueueSize( int size );
}
