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
package org.eclipse.wb.internal.rcp.databinding.parser;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Default bean for replace user wrong beans.
 *
 * @author lobas_av
 * @coverage bindings.rcp.parser
 */
public final class DefaultBean {
	public static final DefaultBean INSTANCE = new DefaultBean();

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Simple property.
	 */
	public String getFoo() {
		return "";
	}

	/**
	 * List property.
	 */
	public List<?> getFooList() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Set property.
	 */
	public Set<?> getFooSet() {
		return Collections.EMPTY_SET;
	}
}