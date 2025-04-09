/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.customize;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Utils for support customizing (API&Beans).
 *
 * @author sablin_aa
 * @coverage swing.customize
 */
public final class JavaInfoState {
	public final Object object;
	public final List<Property> properties = new ArrayList<>();
	public final List<Object> oldValues = new ArrayList<>();
	public final List<Method> getters = new ArrayList<>();
	public final List<Method> setters = new ArrayList<>();
	public final Set<String> changedProperties = new TreeSet<>();
	public final Map<String, Object> changedPropertyValues = new TreeMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private JavaInfoState(JavaInfo javaInfo) {
		object = javaInfo.getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepare information about properties: property, currentValue, "get" method.
	 */
	public static JavaInfoState getState(JavaInfo javaInfo) throws Exception {
		JavaInfoState info = new JavaInfoState(javaInfo);
		Map<String, Property> allProperties = preparePropertiesByTitle(javaInfo);
		for (GenericPropertyDescription description : javaInfo.getDescription().getProperties()) {
			// find property "get" and "set" methods
			Method getter = null;
			Method setter = null;
			for (ExpressionAccessor accessor : description.getAccessorsList()) {
				if (accessor instanceof SetterAccessor setAccessor) {
					getter = setAccessor.getGetter();
					setter = setAccessor.getSetter();
					break;
				}
			}
			// add property information
			if (getter != null) {
				Property property = allProperties.get(description.getTitle());
				info.properties.add(property);
				info.getters.add(getter);
				info.setters.add(setter);
				info.oldValues.add(getter.invoke(info.object));
			}
		}
		return info;
	}

	/**
	 * Prepare table of pair's [title, property].
	 */
	private static Map<String, Property> preparePropertiesByTitle(JavaInfo javaInfo) throws Exception {
		Map<String, Property> properties = new HashMap<>();
		for (Property property : javaInfo.getProperties()) {
			properties.put(property.getTitle(), property);
		}
		return properties;
	}
}
