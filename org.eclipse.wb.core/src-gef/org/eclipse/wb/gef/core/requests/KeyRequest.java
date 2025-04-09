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

import org.eclipse.gef.Request;
import org.eclipse.swt.events.KeyEvent;

/**
 * A {@link Request} that represented key event.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class KeyRequest extends Request {
	/**
	 * Constant used to indicate key event request.
	 */
	public static final String REQ_KEY_EVENT = "key event";
	private final boolean m_pressed;
	private final char m_character;
	private final int m_keyCode;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public KeyRequest(boolean pressed, KeyEvent event) {
		super(REQ_KEY_EVENT);
		m_pressed = pressed;
		m_character = event.character;
		m_keyCode = event.keyCode;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this key pressed event.
	 */
	public boolean isPressed() {
		return m_pressed;
	}

	/**
	 * @return the character represented by the key that was typed.
	 */
	public char getCharacter() {
		return m_character;
	}

	/**
	 * @return the key code of the key that was typed, as defined by the key code constants in class
	 *         <code>SWT</code>.
	 */
	public int getKeyCode() {
		return m_keyCode;
	}

	/**
	 * @return the state of the keyboard modifier keys at the time the event was generated, as defined
	 *         by the key code constants in class <code>SWT</code>.
	 */
	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("KeyRequest(type=");
		buffer.append(getType());
		buffer.append(", pressed=");
		buffer.append(m_pressed);
		buffer.append(", character=");
		buffer.append(m_character);
		buffer.append(", keyCode=");
		buffer.append(m_keyCode);
		buffer.append(")");
		return buffer.toString();
	}
}