/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link FailableBiConsumer} that allows to set options for standard bean
 * properties.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class StandardBeanPropertiesFlaggedRule
		implements FailableBiConsumer<ComponentDescription, Object, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void accept(ComponentDescription componentDescription, Object properties)
			throws Exception {
		// prepare names
		String[] names;
		{
			String namesString = getNames(properties);
			names = StringUtils.split(namesString);
		}
		// check names
		for (String name : names) {
			for (GenericPropertyDescription propertyDescription : componentDescription.getProperties()) {
				String id = propertyDescription.getId();
				if (matchPropertyId(id, name)) {
					configure(propertyDescription);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Configuring
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures given {@link GenericPropertyDescription}.
	 */
	protected abstract void configure(GenericPropertyDescription propertyDescription);

	/**
	 * Extracts the names of the given property.
	 */
	protected abstract String getNames(Object properties);

	////////////////////////////////////////////////////////////////////////////
	//
	// Match
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean matchPropertyId(String id, String name) {
		// check for explicit method name
		if (name.startsWith("m:")) {
			String signaturePrefix = "set" + StringUtils.capitalize(name.substring(2)) + "(";
			return matchAsSetter(id, signaturePrefix);
		}
		// check for explicit field name
		if (name.startsWith("f:")) {
			String fieldName = name.substring(2);
			return matchAsField(id, fieldName);
		}
		// check for full method signature
		if (name.indexOf('(') != -1) {
			return matchAsSetter(id, name);
		}
		// check for template
		if (name.endsWith("*")) {
			String signaturePrefix = "set" + StringUtils.capitalize(name.substring(0, name.length() - 1));
			return matchAsSetter(id, signaturePrefix);
		}
		// try setter
		{
			String signaturePrefix = "set" + StringUtils.capitalize(name) + "(";
			if (matchAsSetter(id, signaturePrefix)) {
				return true;
			}
		}
		// try field name
		if (matchAsField(id, name)) {
			return true;
		}
		// no
		return false;
	}

	private static boolean matchAsSetter(String id, String signaturePrefix) {
		return id.startsWith(signaturePrefix);
	}

	private static boolean matchAsField(String id, String fieldName) {
		return id.equals(fieldName);
	}
}
