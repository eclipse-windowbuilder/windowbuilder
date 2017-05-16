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

import com.google.common.collect.Maps;

import org.eclipse.wb.gef.core.EditPart;

import org.eclipse.swt.SWT;

import java.util.Map;

/**
 * An Object used to communicate with {@link EditPart}s. {@link Request} encapsulates the
 * information {@link EditPart}s need to perform various functions. {@link Request}s are used for
 * obtaining commands, showing feedback, and performing generic operations.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class Request {
  /**
   * Indicates that the user has double-clicked on the receiver. "Open" means different things for
   * different applications. Sometimes it means open a popup dialog of properties, or the
   * Workbench's properties view. Sometimes it means open a sub-diagram.
   */
  public static final String REQ_OPEN = "open";
  /**
   * Indicates selection Requests.
   */
  public static final String REQ_SELECTION = "selection";
  /**
   * Indicates that the receiver of the request is being moved.
   */
  public static final String REQ_MOVE = "move";
  /**
   * Constant used to indicate that a group of existing children are being added to the receiver of
   * the Request.
   */
  public static final String REQ_ADD = "add children";
  /**
   * Indicates that a group of children are being removed from the receiver of the Request.
   */
  public static final String REQ_ORPHAN = "orphan children"; //$NON-NLS-1$
  /**
   * Indicates that an object is to be created by the receiver of the Request.
   */
  public static final String REQ_CREATE = "create child";
  /**
   * Constant used to indicate that the receiver of the Request is being deleted.
   */
  public static final String REQ_DELETE = "delete";
  /**
   * Indicates that an object is to be pasted by the receiver of the Request.
   */
  public static final String REQ_PASTE = "paste";
  /**
   * Constant used to indicate key event request.
   */
  public static final String REQ_KEY_EVENT = "key event";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs an empty {@link Request}.
   */
  public Request() {
  }

  /**
   * Constructs a {@link Request} with the specified <i>type</i>.
   */
  public Request(Object type) {
    setType(type);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object m_type;

  /**
   * Returns the type of the request. The type is often used as a quick way to filter recognized
   * Requests. Once the type is identified, the Request is usually cast to a more specific subclass
   * containing additional data.
   */
  public final Object getType() {
    return m_type;
  }

  /**
   * Sets the type of the Request.
   */
  public final void setType(Object type) {
    m_type = type;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditPart m_target;

  /**
   * Returns the new target that caused sending this {@link Request}.<br>
   * It may be useful to know in old target why it receives "erase target feedback" notification.
   *
   * @return the new target {@link EditPart}.
   */
  public EditPart getTarget() {
    return m_target;
  }

  /**
   * Sets the new target {@link EditPart}.
   */
  public void setTarget(EditPart target) {
    m_target = target;
  }

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
  public final int getStateMask() {
    return m_stateMask;
  }

  /**
   * Sets statemask for this request.
   */
  public final void setStateMask(int stateMask) {
    m_stateMask = stateMask;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "Request(type=" + m_type + ", stateMask=" + m_stateMask + ")";
  }

  /**
   * @return the string presentation of given {@link Object} or "<exception>" if any exception
   *         happened.
   */
  protected static String safeToString(Object o) {
    try {
      return o == null ? null : o.toString();
    } catch (Throwable e) {
      return "<exception>";
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Erase feedback flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_eraseFeedback;

  /**
   * @return <code>true</code> if this {@link Request} is sent as part of erasing feedback. So, we
   *         may skip some checks, for example for mouse location.
   */
  public boolean isEraseFeedback() {
    return m_eraseFeedback;
  }

  public void setEraseFeedback(boolean eraseFeedback) {
    m_eraseFeedback = eraseFeedback;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DND Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_dndFeedback;

  /**
   * @return additional DND feedback flags.
   */
  public int getDNDFeedback() {
    return m_dndFeedback;
  }

  /**
   * Sets additional DND feedback flags.
   */
  public void setDNDFeedback(int dndFeedback) {
    m_dndFeedback = dndFeedback;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<Object, Object> m_arbitraryMap;

  /**
   * Associates the given value with the given key.
   */
  public final void putArbitraryValue(Object key, Object value) {
    if (m_arbitraryMap == null) {
      m_arbitraryMap = Maps.newHashMap();
    }
    m_arbitraryMap.put(key, value);
  }

  /**
   * @return the value to which the given key is mapped, or <code>null</code>.
   */
  public final Object getArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      return m_arbitraryMap.get(key);
    }
    return null;
  }
}