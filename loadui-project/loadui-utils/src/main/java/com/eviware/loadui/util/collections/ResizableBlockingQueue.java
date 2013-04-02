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
package com.eviware.loadui.util.collections;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * An implementation of BlockingQueue which has a mutable capacity.
 * 
 * @author dain.nilsson
 * 
 * @param <E>
 */
public class ResizableBlockingQueue<E> extends LinkedBlockingQueue<E>
{
	private static final long serialVersionUID = 2987868556309474041L;

	private final Object offerLock = new Object();
	private int capacity = Integer.MAX_VALUE;

	public ResizableBlockingQueue( int capacity )
	{
		super();

		this.capacity = capacity;
	}

	public ResizableBlockingQueue()
	{
		this( Integer.MAX_VALUE );
	}

	@Override
	public boolean offer( E e )
	{
		synchronized( offerLock )
		{
			if( size() < capacity )
				return super.offer( e );
		}

		return false;
	}

	/**
	 * Sets the capacity of the BlockingQueue.
	 * 
	 * @param size
	 */
	/**
	 * @param size
	 */
	public void setCapacity( int capacity )
	{
		this.capacity = capacity;
	}

	/**
	 * Gets the capacity of the BlockingQueue.
	 * 
	 * @return
	 */
	public int getCapacity()
	{
		return capacity;
	}
}
