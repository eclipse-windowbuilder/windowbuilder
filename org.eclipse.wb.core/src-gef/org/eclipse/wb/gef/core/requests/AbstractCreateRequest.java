/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.LocationRequest;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class AbstractCreateRequest extends LocationRequest implements DropRequest {
	private Dimension m_size;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractCreateRequest() {
	}

	public AbstractCreateRequest(Object type) {
		super(type);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the size of the object to be created.
	 */
	public Dimension getSize() {
		return m_size;
	}

	/**
	 * Sets the size of the new object.
	 */
	public void setSize(Dimension size) {
		m_size = size;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// State
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Copies state from given {@link Request} into this one.
	 */
	public void copyStateFrom(Request _source) {
		if (_source instanceof AbstractCreateRequest source) {
			setLocation(source.getLocation());
			setSize(source.getSize());
		}
	}
}