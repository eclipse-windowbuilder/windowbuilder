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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.swt.events.KeyEvent;

/**
 * A {@link Request} that represented key event.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class KeyRequest extends Request {
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
    setStateMask(event.stateMask);
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
    buffer.append(", stateMask=");
    buffer.append(getStateMask());
    buffer.append(")");
    return buffer.toString();
  }
}