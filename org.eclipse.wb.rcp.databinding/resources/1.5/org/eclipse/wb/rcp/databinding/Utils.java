/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.rcp.databinding;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.eclipse.core.databinding.BindingException;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
/*package*/final class Utils {
	private static final Object[] EMPTY_ARRAY = new Object[0];
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static Method getMethod(Class<?> beanClass, String propertyName) {
		if (propertyName != null) {
			PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, propertyName);
			if (descriptor != null) {
				return descriptor.getReadMethod();
			}
		}
		return null;
	}
	public static Object invokeMethod(Method method, Class<?> beanClass, Object element) {
		if (method != null && instanceOf(beanClass, element)) {
			try {
				return method.invoke(element, EMPTY_ARRAY);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	public static boolean instanceOf(Class<?> beanClass, Object element) {
		return element != null && beanClass.isAssignableFrom(element.getClass());
	}
	private static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
		} catch (IntrospectionException e) {
			return null;
		}
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			if (descriptor.getName().equals(propertyName)) {
				return descriptor;
			}
		}
		throw new BindingException(MessageFormat.format("Could not find property with name {0} in class {1}", propertyName, beanClass));
	}
}