package com.eviware.loadui.util.remote;

import java.util.List;

public interface ReferenceWrapper
{
	public ReferenceWrapper getSelf();

	public List<String> getImplementedClasses();

	public List<String> getImplementedInterfaces();

	public Object invoke( Class<?> declaringClass, String methodName, Object[] args );
}