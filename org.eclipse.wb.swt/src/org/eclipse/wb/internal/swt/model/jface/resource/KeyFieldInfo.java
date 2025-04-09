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
package org.eclipse.wb.internal.swt.model.jface.resource;

/**
 * Additional class with information about registry key (describe as field).
 *
 * @author lobas_av
 * @coverage swt.model.jface
 */
public final class KeyFieldInfo {
	/**
	 * Key field name.
	 */
	public final String keyName;
	/**
	 * Key source code: <code>qualifiedClassName.fieldName</code>.
	 */
	public final String keySource;
	/**
	 * Key field value.
	 */
	public final String keyValue;
	/**
	 * Registry value for current key. Maybe is <code>null</code>.
	 */
	public Object value;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public KeyFieldInfo(Class<?> declaringClass, String keyName, String keyValue) {
		this.keyName = keyName;
		keySource = declaringClass.getName() + "." + keyName;
		this.keyValue = keyValue;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		return keyName.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof KeyFieldInfo info) {
			return keyName.equals(info.keyName);
		}
		return false;
	}
}