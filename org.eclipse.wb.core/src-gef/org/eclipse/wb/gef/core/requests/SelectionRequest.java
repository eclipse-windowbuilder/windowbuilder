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

import org.eclipse.wb.gef.core.EditPart;

import org.eclipse.swt.SWT;

/**
 * A {@link Request} to select an {@link EditPart}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SelectionRequest extends LocationRequest {
	private int m_lastButtonPressed;

	////////////////////////////////////////////////////////////////////////////
	//
	// State mask
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_stateMask;

	/**
	 * Returns <code>true</code> if the ALT key is currently pressed.
	 */
	public final boolean isAltKeyPressed() {
		return (m_stateMask & SWT.ALT) != 0;
	}

	/**
	 * Returns <code>true</code> if the CTRL key is currently pressed.
	 */
	public final boolean isControlKeyPressed() {
		return (m_stateMask & SWT.CONTROL) != 0;
	}

	/**
	 * Returns <code>true</code> if the SHIFT key is currently pressed.
	 */
	public final boolean isShiftKeyPressed() {
		return (m_stateMask & SWT.SHIFT) != 0;
	}

	/**
	 * Returns <code>true</code> if the left mouse button is pressed.
	 */
	public final boolean isLeftMouseButtonPressed() {
		return (m_stateMask & SWT.BUTTON1) != 0;
	}

	/**
	 * Returns <code>true</code> if the right mouse button is pressed.
	 */
	public final boolean isRightMouseButtonPressed() {
		return (m_stateMask & SWT.BUTTON3) != 0;
	}

	/**
	 * Returns <code>true</code> if any mouse button is currently pressed.
	 */
	public final boolean isAnyMouseButtonPressed() {
		return (m_stateMask & (SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3)) != 0;
	}

	/**
	 * Returns statemask for this request.
	 */
	public final int getModifiers() {
		return m_stateMask;
	}

	/**
	 * Sets statemask for this request.
	 */
	public final void setModifiers(int stateMask) {
		m_stateMask = stateMask;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the last button that was pressed. This is useful if there is more than one mouse button
	 * pressed and the most recent button pressed needs to be identified.
	 */
	public int getLastButtonPressed() {
		return m_lastButtonPressed;
	}

	/**
	 * Sets the last mouse button that was pressed.
	 */
	public void setLastButtonPressed(int lastButtonPressed) {
		m_lastButtonPressed = lastButtonPressed;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("SelectionRequest(type=");
		buffer.append(getType());
		buffer.append(", location=");
		buffer.append(getLocation());
		buffer.append(", stateMask=");
		buffer.append(getModifiers());
		buffer.append(", button=");
		buffer.append(m_lastButtonPressed);
		buffer.append(")");
		return buffer.toString();
	}
}