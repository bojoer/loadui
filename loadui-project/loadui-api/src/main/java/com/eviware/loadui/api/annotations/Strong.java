package com.eviware.loadui.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a hint to the UI that the element of a type with the Strong
 * annotation should be emphasized.
 * 
 * @author dain.nilsson
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Strong
{
}
