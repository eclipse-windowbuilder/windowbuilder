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
package org.eclipse.wb.internal.core.utils.state;

/**
 * Information about some warning happened in editor.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class EditorWarning {
  private final String m_message;
  private final Throwable m_exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorWarning(String message) {
    this(message, null);
  }

  public EditorWarning(String message, Throwable exception) {
    m_message = message;
    m_exception = exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the message of warning.
   */
  public String getMessage() {
    return m_message;
  }

  /**
   * @return the exception associated with this warning, may be <code>null</code>.
   */
  public Throwable getException() {
    return m_exception;
  }
}
