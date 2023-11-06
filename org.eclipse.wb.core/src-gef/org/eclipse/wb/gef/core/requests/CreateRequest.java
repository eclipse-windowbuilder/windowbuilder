/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core.requests;

import org.eclipse.gef.RequestConstants;

/**
 * A {@link Request} to create a new object.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CreateRequest extends AbstractCreateRequest {
	private static final int SNAP_TO = 16;
	private final ICreationFactory m_factory;
	private Object m_newObject;
	private Object m_selectObject;
	private int m_flags = 0;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a {@link CreateRequest} with the specified <i>type</i> and <i>factory</i>.
	 */
	public CreateRequest(ICreationFactory factory) {
		super(RequestConstants.REQ_CREATE);
		m_factory = factory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the new object from the factory and returns that object.
	 */
	public Object getNewObject() {
		if (m_newObject == null) {
			m_newObject = m_factory.getNewObject();
			m_selectObject = m_newObject;
		}
		return m_newObject;
	}

	/**
	 * @return the object that should be selected after finishing create operation. By default return
	 *         same as {@link #getNewObject()}.
	 */
	public Object getSelectObject() {
		return m_selectObject;
	}

	/**
	 * Sets the object that should be selected after finishing create operation.
	 */
	public void setSelectObject(Object object) {
		m_selectObject = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Snap to horizontal axis
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Used to set whether snap-to is being performed.
	 *
	 * @param value <code>true</code> if the request is for a creation with snap-to
	 *              enabled
	 */
	public void setSnapToEnabled(boolean value) {
		m_flags = value ? m_flags | SNAP_TO : m_flags & ~SNAP_TO;
	}

	/**
	 * Returns <code>true</code> if snap-to is enabled
	 *
	 * @return <code>true</code> if the request is for a creation with snap-to
	 *         enabled
	 */
	public boolean isSnapToEnabled() {
		return (m_flags & SNAP_TO) != 0;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("CreateRequest(type=");
		buffer.append(getType());
		buffer.append(", flags=");
		buffer.append(m_flags);
		buffer.append(", location=");
		buffer.append(getLocation());
		buffer.append(", size=");
		buffer.append(getSize());
		buffer.append(", factory=");
		buffer.append(m_factory);
		if (m_factory != null) {
			buffer.append("[object=");
			buffer.append(safeToString(m_factory.getNewObject()));
			buffer.append("]");
		}
		buffer.append(")");
		return buffer.toString();
	}

	/**
	 * @return the string presentation of given {@link Object} or "<exception>" if
	 *         any exception happened.
	 */
	protected static String safeToString(Object o) {
		try {
			return o == null ? null : o.toString();
		} catch (Throwable e) {
			return "<exception>";
		}
	}
}