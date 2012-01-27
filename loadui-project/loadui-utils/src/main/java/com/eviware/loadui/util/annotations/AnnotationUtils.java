package com.eviware.loadui.util.annotations;

import java.lang.annotation.Annotation;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class AnnotationUtils
{
	private static final LoadingCache<ClassAnnotationMapping, Boolean> classAnnotationMapping = CacheBuilder
			.newBuilder().maximumSize( 1024 ).build( new CacheLoader<ClassAnnotationMapping, Boolean>()
			{
				@Override
				public Boolean load( ClassAnnotationMapping mapping ) throws Exception
				{
					Class<?> type = mapping.type;
					while( type != null )
					{
						if( mapping.type.getAnnotation( mapping.annotation ) != null )
							return true;
						type = type.getSuperclass();
					}

					return false;
				}
			} );

	public static boolean hasAnnotation( Object object, Class<? extends Annotation> annotation )
	{
		return classAnnotationMapping.getUnchecked( new ClassAnnotationMapping( object.getClass(), annotation ) );
	}

	private static final class ClassAnnotationMapping
	{
		private final Class<?> type;
		private final Class<? extends Annotation> annotation;

		private ClassAnnotationMapping( Class<?> type, Class<? extends Annotation> annotation )
		{
			this.type = type;
			this.annotation = annotation;
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode( type, annotation );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			ClassAnnotationMapping other = ( ClassAnnotationMapping )obj;
			return Objects.equal( type, other.type ) && Objects.equal( annotation, other.annotation );
		}
	}
}
