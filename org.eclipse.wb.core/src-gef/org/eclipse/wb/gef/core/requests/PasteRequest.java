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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public class PasteRequest extends AbstractCreateRequest {
	/**
	 * Indicates that an object is to be pasted by the receiver of the Request.
	 */
	public static final String REQ_PASTE = "paste";
	private static final int SNAP_TO = 16;
	private final Object m_memento;
	private int m_flags = 0;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PasteRequest(Object memento) {
		super(REQ_PASTE);
		m_memento = memento;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns object with paste info.
	 */
	public Object getMemento() {
		return m_memento;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Created objects
	//
	////////////////////////////////////////////////////////////////////////////
	private List<?> m_objects = Collections.emptyList();

	/**
	 * @return the {@link List} of pasted objects.
	 */
	public List<?> getObjects() {
		return m_objects;
	}

	/**
	 * Sets the {@link List} of pasted objects, these objects will be selected after paste.<br>
	 * It is expected that handler for {@link PasteRequest} will invoke this method.
	 */
	public void setObjects(List<?> objects) {
		m_objects = objects;
	}

	/**
	 * Shortcut for {@link #setObjects(List)} for single object.
	 */
	public void setObject(Object object) {
		List<Object> objects = new ArrayList<>();
		objects.add(object);
		setObjects(objects);
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
		StringBuffer buffer = new StringBuffer("PasteRequest(type=");
		buffer.append(getType());
		buffer.append(", flags=");
		buffer.append(m_flags);
		buffer.append(", location=");
		buffer.append(getLocation());
		buffer.append(", size=");
		buffer.append(getSize());
		buffer.append(", memento=");
		buffer.append(m_memento);
		buffer.append(")");
		return buffer.toString();
	}
}