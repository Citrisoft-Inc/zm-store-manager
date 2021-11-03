package com.citrisoft.util;

import java.util.Properties;

/** Utility methods for class instantiation */
public class ClassUtil
{

	/**
	 * Creates a class instance with an empty argument list
	 *
	 * @param <T> Class or interface to return
	 * @param className Name of the specific class to instantiate
	 * @param classType Class or interface to cast object to
	 * @return T The object instance
	 * @throws InstantiationException for any error creating or casting object
	 */
	public static <T> T getInstance(String className, Class<T> classType)
		throws InstantiationException
	{
		if (className == null || className.equals(""))
			throw new InstantiationException("No class specified.");

		try
		{
			return classType.cast(Class.forName(className).getDeclaredConstructor().newInstance());
		}
		catch (Exception e)
		{
			throw new InstantiationException("Could not instantiate class:" + e.getMessage());
		}
	}

	/**
	 * Creates a class instance passing a properties file as the argument
	 *
	 * @param <T> Class or interface to return
	 * @param className Name of the specific class to instantiate
	 * @param classType Class or interface to cast object to
	 * @param properties Properties necessary for object construction
	 * @return T The object instance
	 * @throws InstantiationException for any error creating or casting object
	 */
	public static <T> T getInstance(String className, Class<T> classType, Properties properties)
		throws InstantiationException
	{
		if (className == null || className.equals(""))
			throw new InstantiationException("No class specified.");

		try
		{
			Object instance = Class.forName(className).getConstructor(Properties.class).newInstance(properties);
			return classType.cast(instance);
		}
		catch (Exception e)
		{
			throw new InstantiationException("Could not instantiate class: " + e.getMessage());
		}
	}

	/**
	 * Convienence function for getting a class and doing a runtime casting check
	 *
	 * @param <T> Class or interface to return
	 * @param className Name of the specific class to instantiate
	 * @param classType Class or interface to cast object to
	 * @throws ClassNotFoundException if specified class does not exist or is not of classType
	 * @throws ClassCastException if specified class does not match requested class or interface
	 * @return Class The requested class, properly cast
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getClass(String className, Class<T> classType)
		throws ClassNotFoundException, ClassCastException
	{
		Class cls = Class.forName(className);

		if (classType.isAssignableFrom(cls))
		{
			return (Class<? extends T>) Class.forName(className);
		}
		else
		{
			throw new ClassCastException("Specified class does not implement return class or interface.");
		}
	}
}
