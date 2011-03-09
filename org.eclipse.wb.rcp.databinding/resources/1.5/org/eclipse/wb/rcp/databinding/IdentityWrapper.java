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

/**
 * Used for wrapping objects that define their own implementations of <code>equals()</code> and
 * <code>hashCode()</code> when putting them in sets or hashmaps to ensure identity comparison.
 * 
 * @author lobas_av
 */
/*package*/final class IdentityWrapper {
	private final Object m_object;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public IdentityWrapper(Object object) {
		m_object = object;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public Object unwrap() {
		return m_object;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		return System.identityHashCode(m_object);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != IdentityWrapper.class) {
			return false;
		}
		IdentityWrapper wrapper = (IdentityWrapper) object;
		return m_object == wrapper.m_object;
	}
}